package com.yapp.gallery.domain.usecase.camera

import com.yapp.gallery.domain.repository.CameraRepository
import com.yapp.gallery.domain.usecase.record.DeleteTempPostUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapConcat
import javax.inject.Inject

class PublishRecordUseCase @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val deleteTempPostUseCase: DeleteTempPostUseCase
) {
    @OptIn(FlowPreview::class)
    operator fun invoke(postId: Long) = cameraRepository.publishRecord(postId).flatMapConcat {
        deleteTempPostUseCase()
    }
}
