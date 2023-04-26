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
import com.yapp.gallery.domain.usecase.camera.PublishRecordUseCase
import com.yapp.gallery.domain.usecase.camera.RegisterPostUseCase
import com.yapp.gallery.domain.usecase.camera.UploadImagesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ResultViewModel @AssistedInject constructor(
    @Assisted private val postId: Long,
    @Assisted private val byteArray: ByteArray? = null,
    @Assisted private val imageList: List<ByteArray> = emptyList(),
    private val uploadImagesUseCase: UploadImagesUseCase,
    private val registerPostUseCase: RegisterPostUseCase,
    private val publishRecordUseCase: PublishRecordUseCase
) : BaseStateViewModel<ResultState, ResultEvent, ResultReduce, ResultSideEffect>(ResultState()) {

    @AssistedFactory
    interface ResultFactory{
        fun create(postId: Long, byteArray: ByteArray? = null, imageList: List<ByteArray> = emptyList()) : ResultViewModel
    }

    init {
        updateState(ResultReduce.SetLoadedData(postId, ImageData(byteArray), imageList))
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        updateState(ResultReduce.UpdateRegisterState(ResultRegisterState.RegisterError("작품 등록에 실패하였습니다.")))
        sendSideEffect(ResultSideEffect.ShowToast("작품 등록에 실패하였습니다."))
    }
    private fun registerPost(){
        viewModelScope.launch(exceptionHandler) {
            updateState(ResultReduce.UpdateRegisterState(ResultRegisterState.RegisterLoading))
            with(viewState.value){
                val tempList = if (byteArray != null) listOf(byteArray) else imageList
                uploadImagesUseCase(postId, tempList)
                    .collectLatest {
                        uploadPost(it)
                    }
            }
        }
    }

    private suspend fun uploadPost(uriList: List<String>){
        with(viewState.value){
            // 작품 등록
            registerPostUseCase(postId, uriList, authorName, postName, tagList)
                .collectLatest {
                    // 전시 퍼블리시 및 TempPost 지우기
                    publishRecordUseCase(postId)
                        .collectLatest {
                            sendSideEffect(ResultSideEffect.NavigateToHome)
                        }
                }
        }
    }

    override fun handleEvents(event: ResultEvent) {
        when(event){
            is ResultEvent.OnClickRegister -> {
                sendSideEffect(ResultSideEffect.ShowBottomSheet)
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
                    // TODO : 태그는 무조건 #으로 시작해야 하는지?
                    val check = if(tempTag.startsWith("#")){
                        // tagList 안에 있는지 판별
                        viewState.value.tagList.contains(tempTag)
                    } else {
                        viewState.value.tagList.contains("#${tempTag}")
                    }
                    if (!check){
                        val tag = if(tempTag.startsWith("#")) tempTag else "#${tempTag}"
                        updateState(ResultReduce.AddTempTag(tag))
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
                updateState(ResultReduce.UpdateRegisterDialogShown(true))
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
                    captureData = reduce.imageData,
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
                    registerDialogShown = reduce.shown
                )
            }
            is ResultReduce.UpdateRegisterState -> state.copy(registerState = reduce.state)
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