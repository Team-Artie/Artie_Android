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
) : BaseStateViewModel<LoginState, LoginEvent, LoginReduce, LoginSideEffect>(LoginState.Initial) {
    private fun postKakaoLogin(accessToken: String) {
        tokenKakaoLoginUseCase(accessToken)
            .onEach {
                updateState(LoginReduce.TokenSuccess(it))
                firebaseTokenLogin(it, LoginType.Kakao)
            }
            .catch {
                Timber.e("Login 오류 : ${it.message}")
                updateState(LoginReduce.LoginError(it.message))
            }
            .launchIn(viewModelScope)
    }

    private fun postNaverLogin(accessToken: String) {
        tokenNaverLoginUseCase(accessToken)
            .onEach {
                updateState(LoginReduce.TokenSuccess(it))
                firebaseTokenLogin(it, LoginType.Naver)
            }
            .catch {
                Timber.e("Login 오류 : ${it.message}")
                updateState(LoginReduce.LoginError(it.message))
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
                updateState(LoginReduce.LoginError(it.message))
            }
    }



    private fun createUser(firebaseUserId: String) {
        createUserUseCase(firebaseUserId)
            .onEach {
                sendSideEffect(LoginSideEffect.NavigateToHome)
                updateState(LoginReduce.LoginSuccess(it))
            }
            .catch {
                Timber.e("Login 오류 : ${it.message}")
                updateState(LoginReduce.LoginError(it.message))
            }
            .launchIn(viewModelScope)
    }

    private fun setLoginInfo(firebaseId: String, token : String, loginType: LoginType) {
        setLoginInfoUseCase(getLoginTypeToString(loginType), token)
            .onEach { createUser(firebaseId) }
            .launchIn(viewModelScope)
    }


    private fun getIsNotLoading(): Boolean = viewState.value is LoginState.Initial || viewState.value is LoginState.LoginError

    private fun getLoginTypeToString(loginType: LoginType) : String{
        return when(loginType){
            LoginType.Google -> "google"
            LoginType.Kakao -> "kakao"
            LoginType.Naver -> "naver"
            LoginType.None -> "none"
        }
    }

    override fun handleEvents(event: LoginEvent) {
        when(event){
            is LoginEvent.OnGoogleLogin -> {
                if (getIsNotLoading()){
                    sendSideEffect(LoginSideEffect.LaunchGoogleLauncher)
                }
            }
            is LoginEvent.OnKakaoLogin -> {
                if (getIsNotLoading()){
                    sendSideEffect(LoginSideEffect.LaunchKakaoLauncher)
                }
            }
            is LoginEvent.OnNaverLogin -> {
                if (getIsNotLoading()){
                    sendSideEffect(LoginSideEffect.LaunchNaverLauncher)
                }
            }
            is LoginEvent.OnLoginFailure -> {
                updateState(LoginReduce.LoginError(event.message))
            }
            is LoginEvent.OnCreateGoogleUser -> {
                updateState(LoginReduce.TokenSuccess(event.idToken))
                setLoginInfo(event.firebaseId, event.idToken, LoginType.Google)
            }
            is LoginEvent.OnCreateKakaoUser -> {
                postKakaoLogin(event.accessToken)
            }
            is LoginEvent.OnCreateNaverUser -> {
                postNaverLogin(event.accessToken)
            }
        }
    }

    override fun reduceState(state: LoginState, reduce: LoginReduce): LoginState {
        return when(reduce){
            is LoginReduce.TokenSuccess -> LoginState.TokenSuccess(reduce.token)
            is LoginReduce.LoginSuccess -> LoginState.LoginSuccess(reduce.id)
            is LoginReduce.LoginError -> LoginState.LoginError(reduce.message)
        }
    }
}
