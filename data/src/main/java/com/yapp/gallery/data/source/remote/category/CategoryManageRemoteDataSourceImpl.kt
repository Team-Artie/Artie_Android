package com.yapp.gallery.data.source.remote.category

import com.yapp.gallery.data.api.ArtieService
import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import com.yapp.gallery.data.model.CategoryBody
import com.yapp.gallery.domain.entity.category.CategoryPost
import com.yapp.gallery.domain.entity.home.CategoryItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CategoryManageRemoteDataSourceImpl @Inject constructor(
    private val artieService: ArtieService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryManageRemoteDataSource{
    override fun getCategoryPost(
        pageNum: Int, size: Int, categoryId: Long
    ): Flow<CategoryPost> = flow {
        emit(artieService.getCategoryPost(page = pageNum, size = size, id = categoryId))
    }.flowOn(ioDispatcher)

    override fun editCategory(
        categoryId: Long, editedName: String
    ): Flow<Boolean> = flow {
        emit(artieService.editCategory(categoryId, CategoryBody(editedName)).isSuccessful)
    }.flowOn(ioDispatcher)

    override fun deleteCategory(
        categoryId: Long
    ): Flow<Boolean> = flow {
        emit(artieService.deleteCategory(categoryId).isSuccessful)
    }.flowOn(ioDispatcher)

    override fun editCategorySequence(
        categoryList: List<CategoryItem>
    ): Flow<Boolean> = flow {
        emit(artieService.changeCategorySequence(categoryList).isSuccessful)
    }.flowOn(ioDispatcher)
}