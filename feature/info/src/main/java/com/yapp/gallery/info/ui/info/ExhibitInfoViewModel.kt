package com.yapp.gallery.info.ui.info


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import timber.log.Timber

class ExhibitInfoViewModel @AssistedInject constructor(
    private val getValidTokenUseCase: GetValidTokenUseCase,
    private val connectionProvider: ConnectionProvider,
    @Assisted private val accessToken: String?,
) : BaseStateViewModel<ExhibitInfoState, ExhibitInfoEvent, ExhibitInfoReduce, ExhibitInfoSideEffect>(ExhibitInfoState.Initial) {

    @AssistedFactory
    interface InfoFactory {
        fun create(accessToken: String?): ExhibitInfoViewModel
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
                    updateState(ExhibitInfoReduce.Disconnected)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadWithValidToken(){
        accessToken?.let {
            updateState(ExhibitInfoReduce.Connected(it))
            Timber.e("accessToken Received : $it")
        } ?: run {
            getValidTokenUseCase()
                .catch {
                    updateState(ExhibitInfoReduce.Disconnected)
                }
                .onEach {
                    updateState(ExhibitInfoReduce.Connected(it))
                }
                .launchIn(viewModelScope)
        }
    }

    override fun handleEvents(event: ExhibitInfoEvent) {
        when(event){
            is ExhibitInfoEvent.OnLoadAgain -> initLoad()
            is ExhibitInfoEvent.OnWebViewClick -> {
                when(event.action){
                    "NAVIGATE_TO_EXHIBIT_EDIT" -> {
                        event.payload?.let { p ->
                            sendSideEffect(ExhibitInfoSideEffect.NavigateToEdit(p))
                        }
                    }
                    "NAVIGATE_TO_CAMERA" -> {
                        sendSideEffect(ExhibitInfoSideEffect.NavigateToCamera)
                    }
                    "NAVIGATE_TO_GALLERY" -> {
                       sendSideEffect(ExhibitInfoSideEffect.NavigateToGallery)
                    }
                    "GO_BACK" -> {
                        sendSideEffect(ExhibitInfoSideEffect.PopBackStack)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun reduceState(state: ExhibitInfoState, reduce: ExhibitInfoReduce): ExhibitInfoState {
        return when(reduce){
            is ExhibitInfoReduce.Connected -> {
                ExhibitInfoState.Connected(reduce.idToken)
            }
            is ExhibitInfoReduce.Disconnected -> {
                ExhibitInfoState.Disconnected
            }
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: InfoFactory,
            accessToken: String?
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(accessToken) as T
            }
        }
    }
}