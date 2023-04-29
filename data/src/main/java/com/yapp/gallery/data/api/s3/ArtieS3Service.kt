package com.yapp.gallery.data.api.s3

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Url

interface ArtieS3Service{
    @PUT
    suspend fun uploadImage(
        @Url url: String,
        @Header("Content-Type") contentType: String = "image/jpeg",
        @Body file : RequestBody
    ): Response<Unit>
}