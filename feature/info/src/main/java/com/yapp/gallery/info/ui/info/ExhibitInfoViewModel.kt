package com.yapp.gallery.info.ui.info


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.provider.ConnectionProvider
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.ExhibitInfoEvent
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.ExhibitInfoReduce
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.ExhibitInfoSideEffect
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.ExhibitInfoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExhibitInfoViewModel @Inject constructor(
    private val getValidTokenUseCase: GetValidTokenUseCase,
    private val connectionProvider: ConnectionProvider,
    savedStateHandle: SavedStateHandle
) : BaseStateViewModel<ExhibitInfoState, ExhibitInfoEvent, ExhibitInfoReduce, ExhibitInfoSideEffect>(ExhibitInfoState.Initial) {

    private val exhibitId = checkNotNull(savedStateHandle.get<Long>("exhibitId"))
    private val idToken = savedStateHandle.get<String>("idToken")
//    @AssistedFactory
//    interface InfoFactory {
//        fun create(accessToken: String?): ExhibitInfoViewModel
//    }

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
        idToken?.let {
            Timber.e("idToken Received : $it")
            updateState(ExhibitInfoReduce.Connected(it))
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
//        getValidTokenUseCase()
//            .catch {
//                updateState(ExhibitInfoReduce.Disconnected)
//            }
//            .onEach {
//                updateState(ExhibitInfoReduce.Connected(it))
//            }
//            .launchIn(viewModelScope)
//        accessToken?.let {
//            updateState(ExhibitInfoReduce.Connected(it))
//            Timber.e("accessToken Received : $it")
//        } ?: run {
//
//        }
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
                        // Todo : 추후에 아이디 값 적용
                        sendSideEffect(ExhibitInfoSideEffect.NavigateToCamera(exhibitId))
                    }
                    "NAVIGATE_TO_GALLERY" -> {
                        event.payload?.let {
                            val json = JSONObject(it)
                            val count = json.getInt("count")
                            sendSideEffect(ExhibitInfoSideEffect.NavigateToGallery(exhibitId, count))
                        } ?: run{
                            sendSideEffect(ExhibitInfoSideEffect.NavigateToGallery(exhibitId))
                        }
                    }
                    "GO_BACK" -> {
                        sendSideEffect(ExhibitInfoSideEffect.PopBackStack)
                    }
                    "OPEN_NEW_WINDOW" -> {
                        event.payload?.let {
                            val json = JSONObject(it)
                            val url = json.getString("url")
                            if (url.startsWith("http://") || url.startsWith("https://")){
                                sendSideEffect(ExhibitInfoSideEffect.ShowWebPage(url))
                            }
                        }
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

//    companion object {
//        fun provideFactory(
//            assistedFactory: InfoFactory,
//            accessToken: String?
//        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                return assistedFactory.create(accessToken) as T
//            }
//        }
//    }
}