package com.yapp.gallery.data.repository

import com.yapp.gallery.data.BuildConfig
import com.yapp.gallery.data.source.remote.camera.CameraRemoteDataSource
import com.yapp.gallery.domain.repository.CameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import javax.inject.Inject

class CameraRepositoryImpl @Inject constructor(
    private val cameraRemoteDataSource: CameraRemoteDataSource
) : CameraRepository {
    override fun uploadImages(
        postId: Long, imageList: List<ByteArray>
    ): Flow<List<String>> {
        return cameraRemoteDataSource.getPreSignedUrl(postId,
            List(imageList.size) { index -> "artie${postId}_${index+1}" }
        ).map { s3Info ->
            val urlList = s3Info.url
            val resultList = mutableListOf<String>()
            urlList.forEachIndexed { index, info ->
                cameraRemoteDataSource.uploadImage(
                    image = imageList[index],
                    url = info.url,
                    imageName = "artie${postId}_${index + 1}"
                ).singleOrNull()?.let {
                    if (it) resultList.add(info.imageKey)
                    else throw Exception("Failed to upload image")
                }?: run {
                    throw Exception("Failed to upload image")
                }
            }
            resultList.toList()
        }
    }

    override fun registerPost(
        artist: String, imageList: List<String>, name: String, postId: Long, tags: List<String>,
    ): Flow<Unit> = flow {
        imageList.forEach {
            cameraRemoteDataSource.registerPost(artist, it, name, postId, tags).collect()
        }
    }

    override fun registerOnlyImages(
        postId: Long, imageList: List<String>
    ): Flow<Unit> {
        return cameraRemoteDataSource.registerOnlyImages(postId, imageList).map {  }
    }
}