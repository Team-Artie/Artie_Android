package com.yapp.gallery.domain.repository

import kotlinx.coroutines.flow.Flow

interface CameraRepository {
    fun uploadImages(postId: Long, imageList: List<ByteArray>) : Flow<List<String>>
    fun registerPost(artist: String, imageList: List<String>, name: String, postId: Long, tags: List<String>) : Flow<Unit>
    fun registerOnlyImages(postId: Long, imageList: List<String>) : Flow<Unit>
    fun saveImageToGallery(postId: Long, image: ByteArray) : Flow<Unit>
}