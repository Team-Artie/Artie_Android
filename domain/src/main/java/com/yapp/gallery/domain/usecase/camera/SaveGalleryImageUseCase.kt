package com.yapp.gallery.domain.usecase.camera

import com.yapp.gallery.domain.repository.CameraRepository
import javax.inject.Inject

class SaveGalleryImageUseCase @Inject constructor(
    private val cameraRepository: CameraRepository
) {
    operator fun invoke(postId: Long, image: ByteArray) = cameraRepository.saveImageToGallery(postId, image)
}