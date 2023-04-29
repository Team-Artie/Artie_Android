package com.yapp.gallery.home.ui.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.home.ui.home.HomeContract.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import timber.log.Timber

class HomeViewModel @AssistedInject constructor(
    private val getValidTokenUseCase: GetValidTokenUseCase,
    private val connectionProvider: ConnectionProvider,
    @Assisted private val accessToken: String?
) : BaseStateViewModel<HomeState, HomeEvent, HomeReduce, HomeSideEffect>(HomeState()) {

    @AssistedFactory
    interface HomeFactory{
        fun create(accessToken: String?) : HomeViewModel
    }

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
        accessToken?.let {
            updateState(HomeReduce.Connected(it))
            Timber.e("accessToken Received: $it")
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


    private fun handleWebViewBridge(action: String, payload: String?) {
        when (action) {
            "NAVIGATE_TO_CALENDAR" -> sendSideEffect(HomeSideEffect.NavigateToCalendar)
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

    companion object{
        fun provideFactory(
            assistedFactory: HomeFactory,
            accessToken: String?
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(accessToken) as T
            }
        }
    }

}