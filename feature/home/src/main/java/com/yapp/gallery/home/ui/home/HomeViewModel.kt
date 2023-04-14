package com.yapp.gallery.home.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetRefreshedTokenUseCase
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.home.ui.home.HomeContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getValidTokenUseCase: GetValidTokenUseCase,
    private val connectionProvider: ConnectionProvider,
    private val savedStateHandle: SavedStateHandle
) : BaseStateViewModel<HomeState, HomeEvent, HomeSideEffect>(HomeState.Initial) {
    init {
        initLoad()
    }

    private fun initLoad(){
        connectionProvider.getConnectionFlow()
            .onEach {
                if (it) {
                    loadWithValidToken()
                } else {
                    updateState { HomeState.Disconnected }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadWithValidToken(){
        savedStateHandle.get<String>("accessToken")?.let {
           updateState { HomeState.Connected(it) }
        } ?: run {
            getValidTokenUseCase()
                .catch {
                    updateState { HomeState.Disconnected }
                }
                .onEach {
                    updateState { HomeState.Connected(it) }
                }
                .launchIn(viewModelScope)
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

    override fun handleEvents(event: HomeEvent) {
        when(event){
            is HomeEvent.OnLoadAgain ->{
                loadWithValidToken()
            }
            is HomeEvent.OnWebViewClick -> {
                handleWebViewBridge(event.action, event.payload)
            }
        }
    }
}