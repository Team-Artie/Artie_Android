package com.yapp.gallery.home.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.domain.usecase.auth.GetRefreshedTokenUseCase
import com.yapp.gallery.home.ui.home.HomeContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRefreshedTokenUseCase: GetRefreshedTokenUseCase
) : BaseStateViewModel<HomeState, HomeEvent, HomeSideEffect>() {
    override val initialState: HomeState = HomeState.Initial

//    private var _homeSideEffect = Channel<NavigatePayload>()
//    val homeSideEffect = _homeSideEffect.receiveAsFlow()
//
//    private val _homeState = MutableStateFlow<WebViewState>(WebViewState.Initial)
//    val homeState : StateFlow<WebViewState>
//        get() = _homeState

    init {
        loadWithRefreshedToken()
    }

    private fun loadWithRefreshedToken(){
        viewModelScope.launch {
            runCatching { getRefreshedTokenUseCase() }
                .onSuccess {
                    sendEvent(HomeEvent.OnConnect(it))
                }
                .onFailure {
                    sendEvent(HomeEvent.OnLoadAgain)
                }
        }
    }


    private fun handleWebViewBridge(action: String, payload: String?){
        when(action){
            "NAVIGATE_TO_CALENDAR" -> sendSideEffect(HomeSideEffect.NavigateToCalendar)
            "NAVIGATE_TO_EDIT" -> sendSideEffect(HomeSideEffect.NavigateToRecord)
            "NAVIGATE_TO_MY" -> sendSideEffect(HomeSideEffect.NavigateToProfile)
            "NAVIGATE_TO_EXHIBITION_DETAIL" -> {
                payload?.let { p ->
                    val exhibitId = JSONObject(p).getLong("id")
                    sendSideEffect(HomeSideEffect.NavigateToInfo(exhibitId))
                }
            }
        }
        Log.e("homeSideEffect", action)
    }

    override fun reduceState(current: HomeState, event: HomeEvent): HomeState {
        return when(event){
            is HomeEvent.OnConnect -> {
                HomeState.Connected(event.idToken)
            }
            is HomeEvent.OnLoadAgain -> {
                current
            }
            is HomeEvent.OnWebViewClick -> {
                handleWebViewBridge(event.action, event.payload)
                current
            }
            is HomeEvent.OnDisconnect -> {
                HomeState.Disconnected
            }
        }
    }
}