package com.yapp.gallery.profile.ui.category

import androidx.paging.PagingData
import com.yapp.gallery.common.base.ViewModelContract
import com.yapp.gallery.common.model.UiText
import com.yapp.gallery.domain.entity.category.PostContent
import com.yapp.gallery.domain.entity.home.CategoryItem
import kotlinx.coroutines.flow.Flow

class CategoryManageContract {
    data class CategoryManageState(
        val isLoading: Boolean = true,
        val categoryList : List<CategoryItem> = emptyList(),
        val originList : List<CategoryItem> = emptyList(),
        val categoryPostFlowList: List<Flow<PagingData<PostContent>>> = emptyList(),
        val expandedList : List<Boolean> = emptyList()
    ) : ViewModelContract.State

    sealed class CategoryManageEvent : ViewModelContract.Event{
        data class OnExpandClick(val index: Int) : CategoryManageEvent()
        data class OnAddClick(val category: String) : CategoryManageEvent()
        data class OnDeleteClick(val categoryItem : CategoryItem) : CategoryManageEvent()
        data class OnEditClick(val categoryItem: CategoryItem, val edited: String) : CategoryManageEvent()
        data class CheckEditable(val origin: String, val edited: String) : CategoryManageEvent()
        data class CheckAddable(val category: String) : CategoryManageEvent()
        data class OnReorderCategory(val from: Int, val to: Int) : CategoryManageEvent()
        data class OnExpandLoadError(val position: Int) : CategoryManageEvent()
        object OnDispose : CategoryManageEvent()
    }

    sealed class CategoryManageReduce : ViewModelContract.Reduce {
        data class CategoryListLoaded(val categoryList: List<CategoryItem>) : CategoryManageReduce()
        data class CategoryPostFlowListLoaded(val categoryPostFlowList: List<Flow<PagingData<PostContent>>>) : CategoryManageReduce()
        data class AddCategoryItem(val categoryItem: CategoryItem, val flow: Flow<PagingData<PostContent>>) : CategoryManageReduce()
        data class UpdateCategoryItem(val categoryItem: CategoryItem, val edited: String) : CategoryManageReduce()
        data class DeleteCategoryItem(val categoryItem: CategoryItem) : CategoryManageReduce()
        data class CategoryListLoadError(val msg: String?) : CategoryManageReduce()
        data class ChangeCategoryOrder(val from: Int, val to: Int, val updatedList: List<CategoryItem>) : CategoryManageReduce()
        data class UpdateCategoryExpanded(val index: Int) : CategoryManageReduce()
    }

    sealed class CategoryManageSideEffect : ViewModelContract.SideEffect{
        data class ShowSnackbar(val msg: UiText) : CategoryManageSideEffect()
    }
}