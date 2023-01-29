package com.yapp.gallery.data.api.login

import com.yapp.gallery.data.model.TokenLoginBody
import com.yapp.gallery.domain.entity.login.FirebaseToken
import retrofit2.http.Body
import retrofit2.http.POST

interface ArtieNaverService {
    @POST("/verifyToken")
    suspend fun tokenLogin(@Body tokenLoginBody: TokenLoginBody) : FirebaseToken
}