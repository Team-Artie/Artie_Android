package com.yapp.gallery.profile.ui.nickname

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.domain.usecase.auth.GetUserIdUseCase
import com.yapp.gallery.domain.usecase.profile.UpdateNicknameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NicknameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getUserIdUseCase: GetUserIdUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
) : ViewModel(){
    private var userId : Long? = null
    private val originNickname = savedStateHandle["nickname"] ?: ""

    val nickname = mutableStateOf(originNickname)

    private val _nicknameState = MutableStateFlow<NicknameState>(NicknameState.None)
    val nicknameState : StateFlow<NicknameState>
        get() = _nicknameState

    init {
        getUserIdUseCase()
            .catch { Timber.e("userId is null") }
            .onEach {
                userId = it
            }
            .launchIn(viewModelScope)
    }

    fun changeNickname(editedName: String){
        nickname.value = editedName
        if (editedName == originNickname || editedName.isBlank()){
            _nicknameState.value = NicknameState.None
        } else if (editedName.length in 1..10){
            _nicknameState.value = NicknameState.Normal(editedName)
        } else {
            _nicknameState.value = NicknameState.Error("닉네임은 10자 이하이어야 합니다.")
        }
    }

    fun updateNickname(){
        userId?.let {id ->
            updateNicknameUseCase(id, nickname.value)
                .catch {  }
                .onEach {  }
                .launchIn(viewModelScope)
        }
    }
}