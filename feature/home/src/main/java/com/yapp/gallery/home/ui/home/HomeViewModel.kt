package com.yapp.gallery.home.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.home.ui.home.HomeContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getValidTokenUseCase: GetValidTokenUseCase,
    private val connectionProvider: ConnectionProvider,
    private val savedStateHandle: SavedStateHandle
) : BaseStateViewModel<HomeState, HomeEvent, HomeReduce, HomeSideEffect>(HomeState.Initial) {
    init {
        initLoad()
    }

    private fun initLoad(){
        connectionProvider.getConnectionFlow()
            .onEach {
                if (it) {
                    loadWithValidToken()
                } else {
                    updateState(HomeReduce.Disconnected)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadWithValidToken(){
        savedStateHandle.get<String>("accessToken")?.let {
           updateState(HomeReduce.Connected(it))
        } ?: run {
            getValidTokenUseCase()
                .catch {
                    updateState(HomeReduce.Disconnected)
                }
                .onEach {
                    updateState(HomeReduce.Connected(it))
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

    override fun reduceState(state: HomeState, reduce: HomeReduce): HomeState {
        return when(reduce){
            is HomeReduce.Connected -> {
                HomeState.Connected(reduce.idToken)
            }
            is HomeReduce.Disconnected -> {
                HomeState.Disconnected
            }
        }
    }

}