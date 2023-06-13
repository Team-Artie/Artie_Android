package com.yapp.gallery.data.source.remote.camera

import com.yapp.gallery.data.api.ArtieService
import com.yapp.gallery.data.api.s3.ArtieS3Service
import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import com.yapp.gallery.data.model.CreatePostBody
import com.yapp.gallery.data.model.ImageNamesBody
import com.yapp.gallery.data.model.ImageUrisBody
import com.yapp.gallery.data.model.S3Info
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class CameraRemoteDataSourceImpl @Inject constructor(
    private val artieService: ArtieService,
    private val artieS3Service: ArtieS3Service,
    @IoDispatcher private val dispatcher : CoroutineDispatcher
) : CameraRemoteDataSource{
    override fun getPreSignedUrl(
        postId: Long, imageName: List<String>
    ): Flow<S3Info> = flow {
        emit(artieService.getPreSignedUrl(postId, ImageNamesBody(imageName)))
    }.flowOn(dispatcher)

    override fun uploadImage(
        image: ByteArray, imageName: String, url: String
    ): Flow<Boolean> = flow {
        emit(
            artieS3Service.uploadImage(
                url = url,
                file = image.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, image.size)
            ).isSuccessful
        )
    }.flowOn(dispatcher)

    override fun registerPost(
        artist: String,
        imageUri: String,
        name: String,
        postId: Long,
        tags: List<String>,
    ): Flow<Long> = flow {
        emit(artieService.registerPost(CreatePostBody(artist, imageUri, name, postId, tags)).id)
    }.flowOn(dispatcher)

    override fun registerOnlyImages(
        postId: Long, imageList: List<String>
    ): Flow<Int> = flow {
        emit(artieService.registerOnlyImages(postId, ImageUrisBody(imageList)).idList.size)
    }.flowOn(dispatcher)


    override fun publishPost(
        postId: Long
    ): Flow<Boolean> = flow{
        emit(artieService.publishRecord(postId).isSuccessful)
    }.flowOn(dispatcher)
}