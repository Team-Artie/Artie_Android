package com.yapp.gallery.camera.ui.result

import android.net.Uri
import androidx.core.net.toUri
import com.yapp.gallery.camera.model.ImageData
import com.yapp.gallery.common.base.ViewModelContract

class ResultContract {
    data class ResultState(
        val postId: Long = 0L,
        val captureData: ImageData? = null,
        val imageList: List<Uri> = listOf(("https://picsum.photos/360/800").toUri()),
        val authorName: String = "",
        val postName: String = "",
        val tempTag: String = "",
        val tagList: List<String> = emptyList()
    ) : ViewModelContract.State

    sealed class ResultEvent : ViewModelContract.Event {
        object OnClickRegister : ResultEvent()
        data class SetAuthorName(val name: String) : ResultEvent()
        data class SetPostName(val name: String) : ResultEvent()
        data class SetTempTag(val tag: String) : ResultEvent()
    }

    sealed class ResultReduce : ViewModelContract.Reduce{
        data class SetLoadedData(val postId: Long, val imageData: ImageData?, val imageList: List<Uri>) : ResultReduce()
        data class UpdateAuthorName(val name: String) : ResultReduce()
        data class UpdatePostName(val name: String) : ResultReduce()
    }

    sealed class ResultSideEffect : ViewModelContract.SideEffect{
        object ShowBottomSheet : ResultSideEffect()
    }
}