package com.yapp.gallery.data.model

data class CreatePostBody(
    val artist: String,
    val imageUri: String,
    val name: String,
    val postId: Long,
    val tags: List<String>
)