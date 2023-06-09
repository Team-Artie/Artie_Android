package com.yapp.gallery.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import com.yapp.gallery.data.source.prefs.AuthPrefsDataSource
import com.yapp.gallery.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val authPrefsDataSource: AuthPrefsDataSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : AuthRepository{
    override fun setLoginType(loginType: String): Flow<Unit> = flow {
        emit(authPrefsDataSource.setLoginType(loginType))
    }.flowOn(dispatcher)

    override fun setIdToken(idToken: String): Flow<Unit> = flow {
        emit(authPrefsDataSource.setIdToken(idToken))
    }.flowOn(dispatcher)

    override fun setUserId(userId: Long): Flow<Unit> = flow {
        emit(authPrefsDataSource.setUserId(userId))
        Log.d("AuthRepositoryImpl", "setUserId: $userId")
    }.flowOn(dispatcher)

    override suspend fun getLoginType(): String? {
        return authPrefsDataSource.getLoginType()
    }

    override suspend fun getIdToken(): String {
        return authPrefsDataSource.getIdToken().firstOrNull() ?: ""
    }

    override fun getUserId(): Flow<Long> {
        return authPrefsDataSource.getUserId().map {
            it ?: throw Exception("User Id is null")
        }
    }

    override suspend fun getRefreshedToken(): String {
        return authPrefsDataSource.getRefreshedToken().also {
            // 리프레시 된 것 새로 저장
            authPrefsDataSource.setIdToken(it)
        }
    }

    override fun deleteLoginInfo(): Flow<Unit> = flow {
        emit(authPrefsDataSource.deleteLoginInfo())
        Log.e("AuthRepositoryImpl", "deleteCompleted")
    }.flowOn(dispatcher)

    // 유효한 토큰 가져오기
    // 토큰 만료된 경우 자동으로 리프레시 토큰 가져옴
    override fun getValidToken(): Flow<String> = callbackFlow {
        auth.currentUser?.getIdToken(false)?.addOnSuccessListener {
            trySend(it.token ?: return@addOnSuccessListener)
        }?.addOnFailureListener {
            Log.e("AuthRepositoryImpl", "getValidToken: ${it.message}")
        }

        awaitClose()
    }
//        return authPrefsDataSource.getIdTokenExpiredTime().map {
//            if (it.isEmpty() || isTokenExpired(it)) {
//                getRefreshedToken()
//            } else {
//                getIdToken()
//            }
//        }.flowOn(dispatcher)


}