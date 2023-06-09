package com.yapp.gallery.domain.usecase.record

import com.yapp.gallery.domain.repository.ExhibitRecordRepository
import javax.inject.Inject

class CreateRecordUseCase @Inject constructor(
    private val repository: ExhibitRecordRepository
) {
    operator fun invoke(name: String, categoryId: Long, postDate: String, attachedLink : String?)
        = repository.createRecord(name, categoryId, postDate, attachedLink)
}