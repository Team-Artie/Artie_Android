package com.yapp.gallery.profile.ui.nickname

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.profile.ui.nickname.NicknameContract.*
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.domain.usecase.profile.UpdateNicknameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class NicknameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
) : BaseStateViewModel<NicknameState, NicknameEvent, NicknameReduce, NicknameSideEffect>(NicknameState()){
    private val originNickname = savedStateHandle.get<String>("nickname")?.also {
        updateState(NicknameReduce.LoadOriginNickname(it))
    }

    private fun changeNickname(editedName: String){
        // Todo : 추가 조건 필요, 특수문자 공백 이런것은 어떻게 처리할지
        val message = if (editedName == originNickname || editedName.length <= 10){
            null
        } else {
            "닉네임은 10자 이하이어야 합니다."
        }
        updateState(NicknameReduce.UpdateNickname(editedName, message))
    }

    private fun updateNickname(){
        updateNicknameUseCase(viewState.value.edited)
            .onEach {
                if (it){
                    updateState(NicknameReduce.UpdateCompleted(true))
                    sendSideEffect(NicknameSideEffect.PopBackStack)
                } else {
                    updateState(NicknameReduce.UpdateFailureDialogShown(true))
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvents(event: NicknameEvent) {
        when (event) {
            is NicknameEvent.OnChangeNickname -> {
                changeNickname(event.editedName)
            }
            is NicknameEvent.OnClickUpdate -> {
                updateNickname()
            }
            is NicknameEvent.OnCloseFailureDialog -> {
                updateState(NicknameReduce.UpdateFailureDialogShown(false))
            }
        }
    }

    override fun reduceState(state: NicknameState, reduce: NicknameReduce): NicknameState {
        return when(reduce){
            is NicknameReduce.LoadOriginNickname -> {
                state.copy(originNickname = reduce.nickname, edited = reduce.nickname)
            }
            is NicknameReduce.UpdateNickname -> {
                state.copy(edited = reduce.nickname, errorMessage = reduce.message)
            }
            is NicknameReduce.UpdateCompleted -> {
                state.copy(completed = reduce.completed)
            }
            is NicknameReduce.UpdateFailureDialogShown -> {
                state.copy(failureDialogShown = reduce.shown)
            }
        }
    }
}