package com.yapp.gallery.data.model

data class S3Info(
    val url: List<S3Url>
){
    data class S3Url(
        val imageKey: String,
        val originalName: String,
        val url: String
    )
}