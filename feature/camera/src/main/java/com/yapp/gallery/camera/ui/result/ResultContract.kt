package com.yapp.gallery.camera.ui.result

import com.yapp.gallery.camera.model.ImageData
import com.yapp.gallery.common.base.ViewModelContract

class ResultContract {
    data class ResultState(
        val postId: Long = 0L,
        val captureData: ImageData? = null,
        val imageList: List<ByteArray> = emptyList(),
        val authorName: String = "",
        val postName: String = "",
        val tempTag: String = "",
        val tagList: List<String> = emptyList(),
        val registerDialogShown : Boolean = false,
        val skip : Boolean = false,
        val registerState: ResultRegisterState = ResultRegisterState.RegisterInitial
    ) : ViewModelContract.State

    sealed class ResultEvent : ViewModelContract.Event {
        object OnClickRegister : ResultEvent()
        data class SetAuthorName(val name: String) : ResultEvent()
        data class SetPostName(val name: String) : ResultEvent()
        data class SetTempTag(val tag: String) : ResultEvent()
        object EnterTag : ResultEvent()
        data class OnDeleteTag(val tag: String) : ResultEvent()
        data class OnRegister(val skip: Boolean) : ResultEvent()
        object OnCancelRegister : ResultEvent()
        object OnConfirmRegister : ResultEvent()
    }

    sealed class ResultReduce : ViewModelContract.Reduce{
        data class SetLoadedData(val postId: Long, val imageData: ImageData?, val imageList: List<ByteArray>) : ResultReduce()
        data class UpdateAuthorName(val name: String) : ResultReduce()
        data class UpdatePostName(val name: String) : ResultReduce()
        data class UpdateTempTag(val tag: String) : ResultReduce()
        data class AddTempTag(val tag: String) : ResultReduce()
        data class DeleteTag(val tag: String) : ResultReduce()
        data class UpdateRegisterDialogShown(val shown: Boolean, val skip: Boolean = false) : ResultReduce()
        data class UpdateRegisterState(val state: ResultRegisterState) : ResultReduce()
    }

    sealed class ResultSideEffect : ViewModelContract.SideEffect{
        object ShowBottomSheet : ResultSideEffect()
        data class ShowToast(val message: String) : ResultSideEffect()
        object NavigateToHome : ResultSideEffect()
    }

    sealed class ResultRegisterState{
        object RegisterInitial : ResultRegisterState()
        object RegisterLoading : ResultRegisterState()
        data class RegisterError(val message: String) : ResultRegisterState()
    }
}