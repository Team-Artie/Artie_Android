package com.yapp.gallery.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.domain.usecase.login.PostTokenLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val tokenLoginUseCase: PostTokenLoginUseCase
): ViewModel(){
    private var _loginState = MutableStateFlow<LoginState>(LoginState.None)
    val loginState : StateFlow<LoginState>
        get() = _loginState

    fun postTokenLogin(accessToken: String){
        viewModelScope.launch {
            runCatching { tokenLoginUseCase(accessToken) }
                .onSuccess {
                    _loginState.value = LoginState.Success(it)
                }
                .onFailure {
                    _loginState.value = LoginState.Failure
                }
        }
    }
}

sealed class LoginState {
    object None : LoginState()
    data class Success(val token: String) : LoginState()
    object Failure : LoginState()
}