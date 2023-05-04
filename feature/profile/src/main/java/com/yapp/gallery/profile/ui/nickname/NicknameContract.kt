package com.yapp.gallery.profile.ui.nickname

import com.yapp.gallery.common.base.ViewModelContract

class NicknameContract {
    data class NicknameState(
        val originNickname: String = "",
        val edited: String = "",
        val completed: Boolean = false,
        val errorMessage: String? = null,
        val failureDialogShown: Boolean = false,
    ) : ViewModelContract.State{
        val canUpdate : Boolean
            get() = edited != originNickname && errorMessage == null && edited.length in 1..10
    }

    sealed class NicknameEvent : ViewModelContract.Event{
        data class OnChangeNickname(val editedName: String) : NicknameEvent()
        object OnClickUpdate : NicknameEvent()
        object OnCloseFailureDialog : NicknameEvent()
    }

    sealed class NicknameReduce : ViewModelContract.Reduce{
        data class LoadOriginNickname(val nickname: String) : NicknameReduce()
        data class UpdateNickname(val nickname: String, val message: String?) : NicknameReduce()
        data class UpdateCompleted(val completed: Boolean) : NicknameReduce()
        data class UpdateFailureDialogShown(val shown: Boolean) : NicknameReduce()
    }

    sealed class NicknameSideEffect : ViewModelContract.SideEffect{
        object PopBackStack : NicknameSideEffect()
    }

}