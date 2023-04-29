package com.yapp.gallery.domain.usecase.camera

import com.yapp.gallery.domain.repository.CameraRepository
import javax.inject.Inject

class RegisterPostUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    operator fun invoke(
        postId: Long,
        imageList: List<String>,
        authorName: String,
        name: String,
        tags: List<String>,
        skip: Boolean,
    ) = if (skip) cameraRepository.registerOnlyImages(postId, imageList)
        else cameraRepository.registerPost(authorName, imageList, name, postId, tags)

}