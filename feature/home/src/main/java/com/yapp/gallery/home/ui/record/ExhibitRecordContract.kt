package com.yapp.gallery.home.ui.record

import com.yapp.gallery.common.base.ViewModelContract
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.domain.entity.home.CategoryItem
import com.yapp.gallery.domain.entity.home.TempPostInfo

class ExhibitRecordContract {
    data class ExhibitRecordState(
        val categoryList: List<CategoryItem> = emptyList(),
        val categoryState: BaseState<Boolean> = BaseState.Loading,
        val exhibitDate : String = "",
        val exhibitName : String = "",
        val exhibitLink : String = "",
        val categorySelect : Long = -1L,
        val deleted: Boolean = false,
        val continuous: Boolean = false,
        val tempPostInfo: TempPostInfo? = null,
        val postId: Long? = null,
        val recordDialogShown : Boolean = false,
        val tempPostDialogShown : Boolean = false,
        val categoryCreateDialogShown : Boolean = false
    ) : ViewModelContract.State

    sealed class ExhibitRecordEvent : ViewModelContract.Event {
        // 임시 저장된 전시 삭제 요청
        object DeleteTempPost : ExhibitRecordEvent()
        // 임시 저장된 전시 이어서 하기
        object ContinueTempPost : ExhibitRecordEvent()
        // 스낵바 취소
        object OnDeleteCancel : ExhibitRecordEvent()
        // 전시 날짜 설정
        data class SetExhibitDate(val date: String) : ExhibitRecordEvent()
        // 전시 이름 설정
        data class SetExhibitName(val name: String) : ExhibitRecordEvent()
        // 카테고리 추가
        data class AddCategory(val name: String) : ExhibitRecordEvent()
        // 카테고리 조건 체크
        data class CheckCategory(val name: String) : ExhibitRecordEvent()
        // 전시 카테고리 설정
        data class SetCategoryId(val categoryId: Long) : ExhibitRecordEvent()
        // 전시 링크 설정
        data class SetExhibitLink(val link: String) : ExhibitRecordEvent()
        // 전시 기록 생성(업데이트) 버튼 클릭
        object OnRecordClick : ExhibitRecordEvent()
        // 전시 기록 생성 (갤러리) 버튼 클릭
        object OnGalleryClick : ExhibitRecordEvent()
        // 전시 기록 생성 (카메라) 버튼 클릭
        object OnCameraClick : ExhibitRecordEvent()
        // 전시 기록 생성 취소
        object OnCancelClick : ExhibitRecordEvent()
    }

    sealed class ExhibitRecordReduce : ViewModelContract.Reduce {
        data class SetCategoryList(val categoryList: List<CategoryItem>) : ExhibitRecordReduce()
        data class SetTempPost(val tempPostInfo: TempPostInfo) : ExhibitRecordReduce()
        object ContinueTempPost : ExhibitRecordReduce()
        object DeleteTempPost : ExhibitRecordReduce()
        data class UpdateTempDialogShown(val shown: Boolean) : ExhibitRecordReduce()
        data class UpdateRecordDialogShown(val shown: Boolean) : ExhibitRecordReduce()
        data class UpdateCategoryId(val categoryId: Long) : ExhibitRecordReduce()
        data class UpdateExhibitName(val name: String) : ExhibitRecordReduce()
        data class UpdateExhibitDate(val date: String) : ExhibitRecordReduce()
        data class UpdateExhibitLink(val link: String) : ExhibitRecordReduce()
        data class AddCategoryItem(val categoryItem: CategoryItem) : ExhibitRecordReduce()
        data class UpdateCategoryState(val categoryState: BaseState<Boolean>) : ExhibitRecordReduce()
        data class CreateRecord(val postId: Long) : ExhibitRecordReduce()
    }

    sealed class ExhibitRecordSideEffect : ViewModelContract.SideEffect {
        data class NavigateToGallery(val postId: Long) : ExhibitRecordSideEffect()
        data class NavigateToCamera(val postId: Long) : ExhibitRecordSideEffect()
//        object NavigateToHome : ExhibitRecordSideEffect()
        object ShowSnackBar : ExhibitRecordSideEffect()
    }
}