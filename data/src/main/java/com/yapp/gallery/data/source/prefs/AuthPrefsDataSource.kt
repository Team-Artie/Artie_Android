package com.yapp.gallery.data.source.prefs

import kotlinx.coroutines.flow.Flow

interface AuthPrefsDataSource {
    suspend fun setLoginType(loginType: String)
    suspend fun setIdToken(idToken: String)

    suspend fun setUserId(userId: Long)

    suspend fun getLoginType() : String?

    suspend fun getRefreshedToken() : String
    fun getIdToken() : Flow<String>
    fun getIdTokenExpiredTime() : Flow<String>
    fun getUserId() : Flow<Long?>

    suspend fun deleteLoginInfo()
}