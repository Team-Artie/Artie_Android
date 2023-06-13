package com.yapp.gallery.camera.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.camera.model.ImageData
import com.yapp.gallery.camera.ui.result.ResultContract.ResultEvent
import com.yapp.gallery.camera.ui.result.ResultContract.ResultReduce
import com.yapp.gallery.camera.ui.result.ResultContract.ResultRegisterState
import com.yapp.gallery.camera.ui.result.ResultContract.ResultSideEffect
import com.yapp.gallery.camera.ui.result.ResultContract.ResultState
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.domain.usecase.camera.RegisterPostUseCase
import com.yapp.gallery.domain.usecase.camera.SaveGalleryImageUseCase
import com.yapp.gallery.domain.usecase.camera.UploadImagesUseCase
import com.yapp.gallery.domain.usecase.record.DeleteTempPostUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class ResultViewModel @AssistedInject constructor(
    @Assisted private val postId: Long,
    @Assisted private val byteArray: ByteArray? = null,
    @Assisted private val imageList: List<ByteArray> = emptyList(),
    private val uploadImagesUseCase: UploadImagesUseCase,
    private val registerPostUseCase: RegisterPostUseCase,
    private val deleteTempPostUseCase: DeleteTempPostUseCase,
    private val saveImageUseCase: SaveGalleryImageUseCase,
) : BaseStateViewModel<ResultState, ResultEvent, ResultReduce, ResultSideEffect>(ResultState()) {

    private val captureSaveFlow = MutableSharedFlow<Long>()
    private var lastClickedTime = 0L

    @AssistedFactory
    interface ResultFactory{
        fun create(postId: Long, byteArray: ByteArray? = null, imageList: List<ByteArray> = emptyList()) : ResultViewModel
    }

    init {
        if (imageList.isNotEmpty()){
            updateState(ResultReduce.SetLoadedData(postId, null, imageList))
        } else {
            updateState(ResultReduce.SetLoadedData(postId, ImageData(byteArray), emptyList()))
        }

        captureSaveFlow.onEach {
            val enabled = it - lastClickedTime > 3000L
            if (enabled) {
                lastClickedTime = it
                saveCaptureToGallery()
            }
        }.launchIn(viewModelScope)
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e("throwable : $throwable")
        if (throwable is NullPointerException || throwable.cause is NullPointerException){
            // 전시 삭제 nullPointer -> 문제 없음
            sendSideEffect(ResultSideEffect.NavigateToInfo(postId))
        } else {
            updateState(ResultReduce.UpdateRegisterState(ResultRegisterState.RegisterError("작품 등록에 실패하였습니다.")))
            sendSideEffect(ResultSideEffect.ShowToast("작품 등록에 실패하였습니다."))
        }
    }
    private fun registerPost(){
        viewModelScope.launch(exceptionHandler) {
            updateState(ResultReduce.UpdateRegisterState(ResultRegisterState.RegisterLoading))
            with(viewState.value){
                val tempList = if (captureData?.byteArray != null) listOf(captureData.byteArray) else imageList
                uploadImagesUseCase(postId, tempList)
                    .collectLatest {
                        registerPost(it)
                    }
            }
        }
    }

    private suspend fun registerPost(uriList: List<String>){
        with(viewState.value){
            // 작품 등록
            registerPostUseCase(postId, uriList, authorName, postName, tagList, skip)
                .collectLatest {
                    deleteTempPostUseCase().collectLatest {
                        sendSideEffect(ResultSideEffect.NavigateToInfo(postId))
                    }
                }
        }
    }

    private fun saveCaptureToGallery(){
        saveImageUseCase(viewState.value.postId, byteArray ?: return)
            .catch {
                Timber.e("saveCaptureToGallery error : $it")
                sendSideEffect(ResultSideEffect.ShowToast(it.message.toString()))
            }
            .onEach {
                updateState(ResultReduce.UpdateCaptureSaved(true))
                sendSideEffect(ResultSideEffect.ShowToast("갤러리에 저장되었습니다."))
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvents(event: ResultEvent) {
        when(event){
            is ResultEvent.OnClickRegister -> {
                sendSideEffect(ResultSideEffect.ShowBottomSheet)
            }
            is ResultEvent.OnClickCaptureSave -> {
                if (viewState.value.isSaved){
                    sendSideEffect(ResultSideEffect.ShowToast("이미 저장된 사진입니다."))
                } else {
                    viewModelScope.launch {
                        captureSaveFlow.emit(System.currentTimeMillis())
                    }
                }
            }
            is ResultEvent.SetAuthorName -> {
                updateState(ResultReduce.UpdateAuthorName(event.name))
            }
            is ResultEvent.SetPostName -> {
                updateState(ResultReduce.UpdatePostName(event.name))
            }
            is ResultEvent.SetTempTag -> {
                if (viewState.value.tagList.size >= 5){
                    return
                }
                else updateState(ResultReduce.UpdateTempTag(event.tag))
            }
            is ResultEvent.EnterTag -> {
                val tempTag = viewState.value.tempTag
                if (tempTag.isNotEmpty()){
                    if (!viewState.value.tagList.contains(tempTag)){
                        updateState(ResultReduce.AddTempTag(tempTag))
                    } else {
                        sendSideEffect(ResultSideEffect.ShowToast("이미 존재하는 태그입니다."))
                    }
                } else {
                    sendSideEffect(ResultSideEffect.ShowToast("태그를 입력해주세요."))
                }
            }
            is ResultEvent.OnDeleteTag -> {
                updateState(ResultReduce.DeleteTag(event.tag))
            }
            is ResultEvent.OnRegister -> {
                updateState(ResultReduce.UpdateRegisterDialogShown(true, event.skip))
            }
            is ResultEvent.OnCancelRegister -> {
                updateState(ResultReduce.UpdateRegisterDialogShown(false))
            }
            is ResultEvent.OnConfirmRegister -> {
                registerPost()
            }
        }
    }

    override fun reduceState(state: ResultState, reduce: ResultReduce): ResultState {
        return when(reduce){
            is ResultReduce.SetLoadedData -> {
                state.copy(
                    postId = postId,
                    captureData = reduce.captureData,
                    imageList = reduce.imageList,
                )
            }
            is ResultReduce.UpdateAuthorName -> {
                state.copy(authorName = reduce.name)
            }
            is ResultReduce.UpdatePostName -> {
                state.copy(postName = reduce.name)
            }
            is ResultReduce.UpdateTempTag -> {
                state.copy(tempTag = reduce.tag)
            }
            is ResultReduce.AddTempTag -> {
                state.copy(
                    tempTag = "",
                    tagList = state.tagList + reduce.tag
                )
            }
            is ResultReduce.DeleteTag -> {
                state.copy(
                    tagList = state.tagList - reduce.tag
                )
            }
            is ResultReduce.UpdateRegisterDialogShown -> {
                state.copy(
                    registerDialogShown = reduce.shown,
                    skip = reduce.skip
                )
            }
            is ResultReduce.UpdateRegisterState -> state.copy(registerState = reduce.state)
            is ResultReduce.UpdateCaptureSaved -> state.copy(isSaved = reduce.saved)
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: ResultFactory,
            postId: Long,
            byteArray: ByteArray? = null,
            imageList: List<ByteArray> = emptyList()
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(postId, byteArray, imageList) as T
            }
        }
    }
}