package com.yapp.gallery.data.source.remote.camera

import com.yapp.gallery.data.model.S3Info
import kotlinx.coroutines.flow.Flow

interface CameraRemoteDataSource {
    fun getPreSignedUrl(postId: Long, imageName: List<String>) : Flow<S3Info>
    fun uploadImage(image: ByteArray, imageName: String, url: String) : Flow<Boolean>
    fun registerPost(artist: String, imageUri: String, name: String, postId: Long, tags: List<String>) : Flow<Long>
    fun registerOnlyImages(postId: Long, imageList: List<String>) : Flow<Int>
    fun publishPost(postId: Long) : Flow<Boolean>
}