package com.yapp.gallery.data.source.local.record

import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import com.yapp.gallery.data.room.TempPost
import com.yapp.gallery.data.room.TempPostDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.singleOrNull
import javax.inject.Inject

class ExhibitRecordLocalDataSourceImpl @Inject constructor(
    private val tempPostDao: TempPostDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : ExhibitRecordLocalDataSource {
    override fun insertTempPost(
        postId: Long, name: String, categoryId: Long, postDate: String, postLink: String?
    ) : Flow<Long> = flow {
        tempPostDao.insertTempPost(TempPost(postId, name, categoryId, postDate, postLink))
        emit(postId)
    }.flowOn(dispatcher)

    override fun updateTempPost(
        postId: Long, name: String, categoryId: Long, postDate: String, postLink: String?,
    ): Flow<Long> = flow {
        tempPostDao.updateTempPost(TempPost(postId, name, categoryId, postDate, postLink))
        emit(postId)
    }.flowOn(dispatcher)

    override fun getTempPost(): Flow<TempPost> = flow {
        emit(tempPostDao.getPost())
    }.flowOn(dispatcher)

    override fun deleteTempPost(): Flow<Long> = flow {
        val postId = tempPostDao.getPost().postId

        tempPostDao.deletePost()
        emit(postId)
    }.flowOn(dispatcher)
}