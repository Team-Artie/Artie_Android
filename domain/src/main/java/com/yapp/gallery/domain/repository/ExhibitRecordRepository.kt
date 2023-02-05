package com.yapp.gallery.domain.repository

import com.yapp.gallery.domain.entity.home.CategoryItem
import com.yapp.gallery.domain.entity.home.TempPostInfo
import kotlinx.coroutines.flow.Flow

interface ExhibitRecordRepository {
    fun getCategoryList() : Flow<List<CategoryItem>>
    fun createCategory(category: String) : Flow<Long>
    fun createRecord(name: String, categoryId: Long, postDate: String) : Flow<Long>
    fun getTempPost() : Flow<TempPostInfo>
    fun insertTempPost(postId: Long, name: String, categoryId: Long, postDate: String, postLink: String?) : Flow<Unit>
    fun deleteTempPost() : Flow<Unit>
}