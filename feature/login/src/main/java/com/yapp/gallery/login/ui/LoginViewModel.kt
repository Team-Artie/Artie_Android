package com.yapp.gallery.login.ui

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.domain.usecase.auth.SetLoginInfoUseCase
import com.yapp.gallery.domain.usecase.login.CreateUserUseCase
import com.yapp.gallery.domain.usecase.login.PostKakaoLoginUseCase
import com.yapp.gallery.domain.usecase.login.PostNaverLoginUseCase
import com.yapp.gallery.login.ui.LoginContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val tokenKakaoLoginUseCase: PostKakaoLoginUseCase,
    private val tokenNaverLoginUseCase: PostNaverLoginUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val setLoginInfoUseCase: SetLoginInfoUseCase,
) : BaseStateViewModel<LoginState, LoginEvent, LoginSideEffect>() {

    override val initialState: LoginState = LoginState.Initial
    private fun postKakaoLogin(accessToken: String) {
        tokenKakaoLoginUseCase(accessToken)
            .onEach {
                sendEvent(LoginEvent.OnTokenSuccess(it))
                firebaseTokenLogin(it, LoginType.Kakao)
            }
            .catch {
                Timber.e("Login 오류 : ${it.message}")
                sendEvent(LoginEvent.OnLoginFailure(it.message))
            }
            .launchIn(viewModelScope)
    }

    private fun postNaverLogin(accessToken: String) {
        tokenNaverLoginUseCase(accessToken)
            .onEach {
                sendEvent(LoginEvent.OnTokenSuccess(it))
                firebaseTokenLogin(it, LoginType.Naver)
            }
            .catch {
                Timber.e("Login 오류 : ${it.message}")
                sendEvent(LoginEvent.OnLoginFailure(it.message))
            }
            .launchIn(viewModelScope)
    }

    // 카카오, 네이버 로그인
    private fun firebaseTokenLogin(firebaseToken: String, loginType: LoginType) {
        auth.signInWithCustomToken(firebaseToken)
            .addOnCompleteListener { task ->
                task.result.user?.apply {
                    getIdToken(false).addOnCompleteListener { t ->
                        setLoginInfo(uid, t.result?.token ?: "", loginType)
                    }
                }
            }.addOnFailureListener {
                sendEvent(LoginEvent.OnLoginFailure(it.message))
            }
    }



    private fun createUser(firebaseUserId: String) {
        createUserUseCase(firebaseUserId)
            .onEach {
                sendSideEffect(LoginSideEffect.NavigateToHome)
                sendEvent(LoginEvent.OnLoginSuccess(it))
            }
            .catch {
                Timber.e("Login 오류 : ${it.message}")
                sendEvent(LoginEvent.OnLoginFailure(it.message))
            }
            .launchIn(viewModelScope)
    }

    private fun setLoginInfo(firebaseId: String, token : String, loginType: LoginType) {
        setLoginInfoUseCase(getLoginTypeToString(loginType), token)
            .onEach { createUser(firebaseId) }
            .launchIn(viewModelScope)
    }

    override fun reduceState(current: LoginState, event: LoginEvent): LoginState {
       when (event) {
            is LoginEvent.OnGoogleLogin -> {
                if (!getIsLoading()) {
                    sendSideEffect(LoginSideEffect.LaunchGoogleLauncher)
                }
            }
            is LoginEvent.OnKakaoLogin -> {
                if (!getIsLoading()) {
                    sendSideEffect(LoginSideEffect.LaunchKakaoLauncher)
                }
            }
            is LoginEvent.OnNaverLogin -> {
                if (!getIsLoading()) {
                    sendSideEffect(LoginSideEffect.LaunchNaverLauncher)
                }
            }
            is LoginEvent.OnLoginFailure -> {
                return LoginState.TokenError(event.message)
            }
            is LoginEvent.OnCreateGoogleUser -> {
                setLoginInfo(event.firebaseId, event.idToken, LoginType.Google)
                return LoginState.Loading
            }
            is LoginEvent.OnCreateKakaoUser -> {
                postKakaoLogin(event.accessToken)
                return LoginState.Loading
            }
            is LoginEvent.OnCreateNaverUser -> {
                postNaverLogin(event.accessToken)
                return LoginState.Loading
            }
            is LoginEvent.OnLoginSuccess -> {
                return LoginState.LoginSuccess(event.id)
            }
            is LoginEvent.OnTokenSuccess -> {
                return LoginState.TokenSuccess(event.token)
            }
        }
        return current
    }

    private fun getIsLoading(): Boolean = viewState.value is LoginState.Loading
            || viewState.value is LoginState.TokenSuccess || viewState.value is LoginState.LoginSuccess

    private fun getLoginTypeToString(loginType: LoginType) : String{
        return when(loginType){
            LoginType.Google -> "google"
            LoginType.Kakao -> "kakao"
            LoginType.Naver -> "naver"
            LoginType.None -> "none"
        }
    }


}
