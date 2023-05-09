package com.yapp.gallery.home.ui.home

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.home.ui.home.HomeContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val getValidTokenUseCase: GetValidTokenUseCase,
    private val connectionProvider: ConnectionProvider,
) : BaseStateViewModel<HomeState, HomeEvent, HomeReduce, HomeSideEffect>(HomeState()) {

    init {
        auth.currentUser?.let {
            updateState(HomeReduce.UpdateAfterLogin(true))
            initLoad()
        } ?: run {
            viewModelScope.launch {
                delay(1000)
                sendSideEffect(HomeSideEffect.NavigateToLogin)
            }
        }
    }

    private fun initLoad(){
        connectionProvider.getConnectionFlow()
            .onEach {
                if (it) {
                    // 원래 네트워크 연결이 안 되어있다가 연결된 경우
                    if (viewState.value.connected.not()) loadWithValidToken()
                } else {
                    updateState(HomeReduce.Disconnected)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadWithValidToken(){
//        auth.currentUser?.run {
//            auth.addIdTokenListener(FirebaseAuth.IdTokenListener {
//                getIdToken(false).addOnSuccessListener {
//                    Timber.tag("token").e("token: ${it.token}")
//                    if (viewState.value.idToken != it.token) {
//                        sendSideEffect(HomeSideEffect.LoadWebView(it.token ?: return@addOnSuccessListener))
//                    }
//                    updateState(HomeReduce.Connected(it.token ?: return@addOnSuccessListener)) }
//                }
//            )
//        }
        auth.currentUser?.getIdToken(false)?.addOnSuccessListener {
            it.token?.let {t ->
                if (viewState.value.idToken != t) {
                    sendSideEffect(HomeSideEffect.LoadWebView(t))
                    updateState(HomeReduce.Connected(t))
                }
            }
        }
//        getValidTokenUseCase()
//            .catch {
//                updateState(HomeReduce.Disconnected)
//            }
//            .onEach {
//                updateState(HomeReduce.Connected(it))
//            }
//            .launchIn(viewModelScope)
    }


    private fun handleWebViewBridge(action: String, payload: String?) {
        when (action) {
            "NAVIGATE_TO_EDIT" -> sendSideEffect(HomeSideEffect.NavigateToRecord)
            "NAVIGATE_TO_MY" -> sendSideEffect(HomeSideEffect.NavigateToProfile)
            "NAVIGATE_TO_EXHIBITION_DETAIL" -> {
                payload?.let { p ->
                    val exhibitId = JSONObject(p).getLong("id")
                    val idToken = viewState.value.idToken
                    sendSideEffect(HomeSideEffect.NavigateToInfo(exhibitId, idToken))
                }
            }
        }
        Timber.tag("homeSideEffect").e(action)
    }

    override fun handleEvents(event: HomeEvent) {
        when(event){
            is HomeEvent.CheckToken -> {
                loadWithValidToken()
            }
            is HomeEvent.OnLoadAgain ->{
                loadWithValidToken()
            }
            is HomeEvent.OnWebViewClick -> {
                handleWebViewBridge(event.action, event.payload)
            }
        }
    }

    override fun reduceState(state: HomeState, reduce: HomeReduce): HomeState {
        return when(reduce){
            is HomeReduce.UpdateAfterLogin ->
                state.copy(
                    afterLogin = reduce.afterLogin
                )
            is HomeReduce.Connected ->
                state.copy(
                    idToken = reduce.idToken,
                    connected = true
                )
            is HomeReduce.Disconnected ->
                state.copy(
                    connected = false
                )
        }
    }
}