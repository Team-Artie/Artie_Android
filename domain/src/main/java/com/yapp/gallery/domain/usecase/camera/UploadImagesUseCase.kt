package com.yapp.gallery.domain.usecase.camera

import com.yapp.gallery.domain.repository.CameraRepository
import javax.inject.Inject

class UploadImagesUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    operator fun invoke(postId: Long, imageList: List<ByteArray>) = cameraRepository.uploadImages(postId, imageList)
}