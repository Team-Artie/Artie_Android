package com.yapp.gallery.data.source.remote.record

import com.yapp.gallery.data.api.ArtieService
import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import com.yapp.gallery.data.model.CategoryBody
import com.yapp.gallery.data.model.CreateRecordBody
import com.yapp.gallery.data.utils.changeDateFormat
import com.yapp.gallery.domain.entity.home.CategoryItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ExhibitRecordRemoteDataSourceImpl @Inject constructor(
    private val artieService: ArtieService,
    @IoDispatcher private val dispatcher : CoroutineDispatcher
) : ExhibitRecordRemoteDataSource {
    override fun getCategoryList()
    : Flow<List<CategoryItem>> = flow {
        emit(artieService.getCategoryList())
    }.flowOn(dispatcher)

    override fun createCategory(category: String)
    : Flow<Long> = flow {
        emit(artieService.createCategory(CategoryBody(category)).id)
    }.flowOn(dispatcher)

    override fun createRecord(
        name: String,
        categoryId: Long,
        postDate: String,
        attachedLink: String?
    ): Flow<Long> = flow {
        emit(artieService.createRecord(CreateRecordBody(name, categoryId, changeDateFormat(postDate), attachedLink)).id)
    }.flowOn(dispatcher)

    override fun updateRecord(
        postId: Long,
        name: String,
        categoryId: Long,
        postDate: String,
        attachedLink: String?
    ): Flow<Boolean> = flow{
        emit(artieService.updateRecord(postId, CreateRecordBody(name, categoryId, changeDateFormat(postDate), attachedLink)).isSuccessful)
    }

    override fun deleteRecord(
        postId: Long
    ): Flow<Boolean> = flow {
        emit(artieService.deleteRecord(postId).isSuccessful)
    }
}