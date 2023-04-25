package com.yapp.gallery.profile.ui.category

import com.yapp.gallery.common.base.ViewModelContract
import com.yapp.gallery.common.model.UiText
import com.yapp.gallery.domain.entity.home.CategoryItem

class CategoryManageContract {
    sealed class CategoryManageState : ViewModelContract.State{
        object Initial : CategoryManageState()
        data class Success(val categoryList : List<CategoryItem>) : CategoryManageState()
        object Empty : CategoryManageState()
        data class Failure(val msg: String?) : CategoryManageState()
    }

    sealed class CategoryManageEvent : ViewModelContract.Event{
        data class OnExpandClick(val index: Int) : CategoryManageEvent()
        data class OnAddClick(val category: String) : CategoryManageEvent()
        data class OnDeleteClick(val categoryItem : CategoryItem) : CategoryManageEvent()
        data class OnEditClick(val categoryItem: CategoryItem, val edited: String) : CategoryManageEvent()
        data class CheckEditable(val origin: String, val edited: String) : CategoryManageEvent()
        data class CheckAddable(val category: String) : CategoryManageEvent()
        data class OnReorderCategory(val from: Int, val to: Int) : CategoryManageEvent()
        data class OnExpandLoadError(val position: Int) : CategoryManageEvent()
    }

    sealed class CategoryManageReduce : ViewModelContract.Reduce {
        data class CategoryListLoaded(val categoryList: List<CategoryItem>) : CategoryManageReduce()
        object CategoryListEmpty : CategoryManageReduce()
        data class CategoryListLoadError(val msg: String?) : CategoryManageReduce()
    }

    sealed class CategoryManageSideEffect : ViewModelContract.SideEffect{
    }
}