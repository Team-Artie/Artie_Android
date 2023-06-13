package com.yapp.gallery.profile.ui.signout

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.yapp.gallery.domain.usecase.auth.DeleteLoginInfoUseCase
import com.yapp.gallery.domain.usecase.profile.SignOutUseCase
import com.yapp.gallery.domain.usecase.record.DeleteBothUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignOutViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val deleteBothUseCase: DeleteBothUseCase,
    private val deleteLoginInfoUseCase: DeleteLoginInfoUseCase,
    private val googleSignInClient: GoogleSignInClient,
    private val kakaoClient: UserApiClient,
    private val auth: FirebaseAuth,
    @ApplicationContext val context: Context
) : ViewModel(){
    fun removeInfo(loginType: String){
        viewModelScope.launch {
            deleteBothUseCase()
                .catch {
                    signOutUseCase()
                        .collectLatest{
                            deleteLoginInfoUseCase().collect()
                            signOut(loginType)
                        }
                }
                .collect{
                    signOutUseCase()
                        .collectLatest{
                            deleteLoginInfoUseCase().collect()
                            signOut(loginType)
                        }
                }
        }
    }

    private fun signOut(loginType: String){
        Log.e("loginType", loginType)
        when(loginType){
            "kakao" -> {
                kakaoClient.unlink {
                    auth.currentUser?.delete()
                }
            }
            "naver" -> {
                NidOAuthLogin().callDeleteTokenApi(context, object : OAuthLoginCallback {
                    override fun onError(errorCode: Int, message: String) {
                        onFailure(errorCode, message)
                    }

                    override fun onFailure(httpStatus: Int, message: String) {
                        Log.d("네이버 회원탈퇴", "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                        Log.d("네이버 회원탈퇴", "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
                    }

                    override fun onSuccess() {
                        auth.currentUser?.delete()?.addOnCompleteListener {
                            Log.e("firebase 삭제", it.isSuccessful.toString())
                        }
                    }

                })
            }
            else -> {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    auth.currentUser?.delete()
                }
            }
        }
    }

}