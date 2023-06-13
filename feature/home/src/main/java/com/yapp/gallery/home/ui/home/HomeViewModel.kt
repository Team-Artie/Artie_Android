package com.yapp.gallery.home.ui.home

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.home.ui.home.HomeContract.HomeEvent
import com.yapp.gallery.home.ui.home.HomeContract.HomeReduce
import com.yapp.gallery.home.ui.home.HomeContract.HomeSideEffect
import com.yapp.gallery.home.ui.home.HomeContract.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
//                    updateState(
//                        HomeReduce.Connected(
//                            "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQwZTFkMjM5MDllNzZmZjRhNzJlZTA4ODUxOWM5M2JiOTg4ZjE4NDUiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoi7J2064-Z6rG0IiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FFZEZUcDVBZFN6dUNydjdzSHJNS0tjbWxGWDdnT3Q5QXRWTnFvNHBtb0taPXM5Ni1jIiwiaXNzIjoiaHR0cHM6Ly9zZWN1cmV0b2tlbi5nb29nbGUuY29tL2dhbGxlcnktNTNlNTgiLCJhdWQiOiJnYWxsZXJ5LTUzZTU4IiwiYXV0aF90aW1lIjoxNjg1MDI0NTU0LCJ1c2VyX2lkIjoiOVlJTDJmaGNtN1ZuM0Fvb3lUWG9PdVFEQjE1MiIsInN1YiI6IjlZSUwyZmhjbTdWbjNBb295VFhvT3VRREIxNTIiLCJpYXQiOjE2ODUwMjQ1NTQsImV4cCI6MTY4NTAyODE1NCwiZW1haWwiOiJuZXJ3MTczQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7Imdvb2dsZS5jb20iOlsiMTEzNjk0MTU5NjYzOTk4MDU4NzkwIl0sImVtYWlsIjpbIm5lcncxNzNAZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoiZ29vZ2xlLmNvbSJ9fQ.OhmE5nICCZgDON0fRkHj_nZY7LwSf0na4zyLUNUZe9GMAO6qhVcV4GrkEqIIlLdAdZr9XnHeqkALiqXgI2ADlqXHLjskGNo6RhN3NH3Fds5eeqWEvgWJAbDGY3FLzmc5rms4NiiAzpsJD9YmRw9Gl8qGvH1tviZksAaLeeBab9k4yYI4zlBhO-fJmm-89YwhIb0w5_BdXl0LlLc0BDPtd2toOILFAXt9hcVW3U8hRUhtFx8CdZr90dBysMGaqbhEcIl4mEyTquv1ojdsVOHQ_i2vJRyEgbSljKjk30VkMN1V6CzfXLh24zG6-rwkTMGRTGQc2cNozHrAz0FBNpMcyw\n"
//                        )
//                    )
                    loadWithValidToken()
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

        getValidTokenUseCase()
            .catch {
                updateState(HomeReduce.Disconnected)
            }
            .onEach {
                updateState(HomeReduce.Connected(it))
            }
            .launchIn(viewModelScope)
    }


    private fun handleWebViewBridge(action: String, payload: String?) {
        when (action) {
            "NAVIGATE_TO_EDIT" -> sendSideEffect(HomeSideEffect.NavigateToRecord)
            "NAVIGATE_TO_MY" -> sendSideEffect(HomeSideEffect.NavigateToProfile)
            "NAVIGATE_TO_EXHIBITION_DETAIL" -> {
                payload?.let { p ->
                    val exhibitId = JSONObject(p).getLong("id")
                    viewModelScope.launch{
                        // 토큰 조회 실패 시 기존 토큰으로 보냄
                        // 토큰 조회 성공 시 조회한 토큰으로 보냄
                        getValidTokenUseCase().firstOrNull()?.let { idToken ->
                            Timber.d("token get success : $idToken")
                            sendSideEffect(HomeSideEffect.NavigateToInfo(exhibitId, idToken))
                        } ?: run {
                            sendSideEffect(HomeSideEffect.NavigateToInfo(exhibitId, viewState.value.currentToken))
                        }
                    }
                }
            }
            "REQUEST_REFRESH_TOKEN" -> {
                viewModelScope.launch {
                    getValidTokenUseCase().firstOrNull()?.let {
                        sendSideEffect(HomeSideEffect.SendRefreshToken(it))
                    }
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
//                loadWithValidToken()
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
                    previousToken = state.currentToken,
                    currentToken = reduce.idToken,
                    connected = true,
                )
            is HomeReduce.Disconnected ->
                state.copy(
                    connected = false
                )
        }
    }
}