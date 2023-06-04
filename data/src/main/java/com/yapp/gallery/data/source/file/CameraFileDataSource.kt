package com.yapp.gallery.data.source.file

import kotlinx.coroutines.flow.Flow

interface CameraFileDataSource {
    fun saveImageToGallery(image: ByteArray, fileName: String) : Flow<Unit>
}