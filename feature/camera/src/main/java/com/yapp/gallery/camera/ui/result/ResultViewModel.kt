package com.yapp.gallery.camera.ui.result

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yapp.gallery.camera.model.ImageData
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.camera.ui.result.ResultContract.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ResultViewModel @AssistedInject constructor(
    @Assisted private val postId: Long,
    @Assisted private val byteArray: ByteArray? = null,
    @Assisted private val imageList: List<Uri> = emptyList()
) : BaseStateViewModel<ResultState, ResultEvent, ResultReduce, ResultSideEffect>(ResultState()) {

    @AssistedFactory
    interface ResultFactory{
        fun create(postId: Long, byteArray: ByteArray? = null, imageList: List<Uri> = emptyList()) : ResultViewModel
    }

    init {
        updateState(ResultReduce.SetLoadedData(postId, ImageData(byteArray), imageList))
    }

    override fun handleEvents(event: ResultEvent) {
        when(event){
            is ResultEvent.OnClickRegister -> {

            }
            is ResultEvent.SetAuthorName -> {

            }
            is ResultEvent.SetPostName -> {

            }
            is ResultEvent.SetTempTag -> {

            }
        }
    }

    override fun reduceState(state: ResultState, reduce: ResultReduce): ResultState {
        return when(reduce){
            is ResultReduce.SetLoadedData -> {
                state.copy(
                    captureData = reduce.imageData,
                    imageList = reduce.imageList,
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            assistedFactory: ResultFactory,
            postId: Long,
            byteArray: ByteArray? = null,
            imageList: List<Uri> = emptyList()
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(postId, byteArray, imageList) as T
            }
        }
    }
}