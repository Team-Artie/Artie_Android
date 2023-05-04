package com.yapp.gallery.record.ui

import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.domain.entity.home.CategoryItem
import com.yapp.gallery.domain.entity.home.TempPostInfo
import com.yapp.gallery.domain.usecase.record.*
import com.yapp.gallery.record.ui.ExhibitRecordContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExhibitRecordViewModel @Inject constructor(
    private val getCategoryListUseCase: GetCategoryListUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val createRecordUseCase: CreateRecordUseCase,
    private val getTempPostUseCase: GetTempPostUseCase,
    private val updateRecordUseCase: UpdateBothUseCase,
    private val deleteBothUseCase: DeleteBothUseCase,
) : BaseStateViewModel<ExhibitRecordState, ExhibitRecordEvent, ExhibitRecordReduce, ExhibitRecordSideEffect>(
    ExhibitRecordState()
) {
    private var deleteJob : Job? = null
    init {
        getTempPost()
        getCategoryList()
    }

    // 카테고리 리스트 받기
    private fun getCategoryList(){
        getCategoryListUseCase()
            .onEach {
                updateState(ExhibitRecordReduce.SetCategoryList(it))
            }
            .launchIn(viewModelScope)
    }

    // 임시 저장 확인
    private fun getTempPost(){
        getTempPostUseCase()
            .catch {
                Timber.e("임시 저장된 전시 없음")
            }
            .onEach {
                updateState(ExhibitRecordReduce.SetTempPost(it))
            }
            .launchIn(viewModelScope)
    }

    private fun addCategory(category: String){
        createCategoryUseCase(category)
            .onEach {
                updateState(
                    ExhibitRecordReduce.AddCategoryItem(
                        CategoryItem(it, category, viewState.value.categoryList.size, 0)
                    )
                )
            }
            .launchIn(viewModelScope)
    }


    private fun checkCategory(category: String){
        if (viewState.value.categoryList.find { it.name == category } != null){
            updateState(ExhibitRecordReduce.UpdateCategoryState(BaseState.Error("이미 존재하는 카테고리입니다.")))
        } else if (category.length > 10)
            updateState(ExhibitRecordReduce.UpdateCategoryState(BaseState.Error("카테고리는 10자 이하이어야 합니다.")))
        else
            updateState(ExhibitRecordReduce.UpdateCategoryState(BaseState.Success(true)))
    }

    private fun createOrUpdateRecord(type: CreateType) {
        with(viewState.value){
            // 생성된 적 있음
            if (continuous && postId != null) {
                // 바뀐 여부 체크
                if (checkEdited(this)){
                    updateRecordUseCase(postId, exhibitName, categorySelect, exhibitDate, exhibitLink.ifEmpty { null })
                        .onEach {
                           navigateToCameraGallery(type, it)
                        }
                        .launchIn(viewModelScope)
                }
                else {
                    navigateToCameraGallery(type, postId)
                }


            } else {
                createRecordUseCase(exhibitName, categorySelect, exhibitDate, exhibitLink.ifEmpty { null })
                    .onEach {
                        updateState(ExhibitRecordReduce.CreateRecord(it))
                        if (type == CreateType.ALBUM) {
                            sendSideEffect(ExhibitRecordSideEffect.NavigateToGallery(it))
                        } else {
                            sendSideEffect(ExhibitRecordSideEffect.NavigateToCamera(it))
                        }
                    }
                    .launchIn(viewModelScope)
            }

        }
    }
    private fun deleteRecord(){
        // 로컬, 서버 다 지우기
        deleteJob = viewModelScope.launch {
            // 스낵바 띄우는 시간 4초
            delay(4000)
            yield()
            deleteBothUseCase()
                .catch {
                    Timber.e("delete failure : ${it.message}")
                }
                .collect {
                    Timber.d("delete success")
                    updateState(ExhibitRecordReduce.DeleteTempPost)
                }
        }
    }

    private fun checkEdited(state: ExhibitRecordState) : Boolean{
        return with(state){
            exhibitName != tempPostInfo?.name ||
                    categorySelect != tempPostInfo.categoryId ||
                    exhibitDate != tempPostInfo.postDate ||
                    exhibitLink.ifBlank { null } != tempPostInfo.postLink
        }
    }

    private fun navigateToCameraGallery(type: CreateType, postId: Long){
        if (type == CreateType.ALBUM) {
            sendSideEffect(ExhibitRecordSideEffect.NavigateToGallery(postId))
        } else {
            sendSideEffect(ExhibitRecordSideEffect.NavigateToCamera(postId))
        }
    }

    override fun handleEvents(event: ExhibitRecordEvent) {
        when(event){
            is ExhibitRecordEvent.SetCategoryId -> {
                val res = if (event.categoryId == viewState.value.categorySelect) -1L else event.categoryId
                updateState(ExhibitRecordReduce.UpdateCategoryId(res))
            }
            is ExhibitRecordEvent.SetExhibitName -> updateState(ExhibitRecordReduce.UpdateExhibitName(event.name))
            is ExhibitRecordEvent.SetExhibitDate -> updateState(ExhibitRecordReduce.UpdateExhibitDate(event.date))
            is ExhibitRecordEvent.SetExhibitLink -> updateState(ExhibitRecordReduce.UpdateExhibitLink(event.link))
            is ExhibitRecordEvent.CheckCategory -> checkCategory(event.name)
            is ExhibitRecordEvent.AddCategory -> addCategory(event.name)
            is ExhibitRecordEvent.ContinueTempPost -> updateState(ExhibitRecordReduce.ContinueTempPost)
            is ExhibitRecordEvent.DeleteTempPost -> {
                updateState(ExhibitRecordReduce.UpdateTempDialogShown(false))
                sendSideEffect(ExhibitRecordSideEffect.ShowSnackBar)
                deleteRecord()
            }
            is ExhibitRecordEvent.OnDeleteCancel -> {
                deleteJob?.cancel()
                updateState(ExhibitRecordReduce.ContinueTempPost)
            }
            is ExhibitRecordEvent.OnRecordClick -> updateState(ExhibitRecordReduce.UpdateRecordDialogShown(true))
            is ExhibitRecordEvent.OnCancelClick -> updateState(ExhibitRecordReduce.UpdateRecordDialogShown(false))
            is ExhibitRecordEvent.OnGalleryClick -> createOrUpdateRecord(CreateType.ALBUM)
            is ExhibitRecordEvent.OnCameraClick -> createOrUpdateRecord(CreateType.CAMERA)
        }
    }

    override fun reduceState(
        state: ExhibitRecordState,
        reduce: ExhibitRecordReduce,
    ): ExhibitRecordState {
        return when (reduce) {
            is ExhibitRecordReduce.SetCategoryList -> state.copy(categoryList = reduce.categoryList)
            is ExhibitRecordReduce.SetTempPost -> state.copy(
                postId = reduce.tempPostInfo.postId,
                tempPostInfo = reduce.tempPostInfo,
                tempPostDialogShown = true
            )
            is ExhibitRecordReduce.ContinueTempPost -> state.copy(
                exhibitName = state.tempPostInfo?.name ?: "",
                exhibitDate = state.tempPostInfo?.postDate ?: "",
                exhibitLink = state.tempPostInfo?.postLink ?: "",
                categorySelect = state.tempPostInfo?.categoryId ?: -1L,
                continuous = true,
                tempPostDialogShown = false
            )
            is ExhibitRecordReduce.DeleteTempPost -> state.copy(
                tempPostInfo = null,
                postId = null
            )
            is ExhibitRecordReduce.UpdateTempDialogShown -> state.copy(tempPostDialogShown = reduce.shown)
            is ExhibitRecordReduce.UpdateRecordDialogShown -> state.copy(recordDialogShown = reduce.shown)
            is ExhibitRecordReduce.UpdateCategoryId -> state.copy(categorySelect = reduce.categoryId)
            is ExhibitRecordReduce.UpdateExhibitName -> state.copy(exhibitName = reduce.name)
            is ExhibitRecordReduce.UpdateExhibitDate -> state.copy(exhibitDate = reduce.date)
            is ExhibitRecordReduce.UpdateExhibitLink -> state.copy(exhibitLink = reduce.link)
            is ExhibitRecordReduce.AddCategoryItem -> state.copy(categoryList = state.categoryList + reduce.categoryItem)
            is ExhibitRecordReduce.UpdateCategoryState -> state.copy(categoryState = reduce.categoryState)
            is ExhibitRecordReduce.CreateRecord ->
                state.copy(
                    continuous = true, postId = reduce.postId,
                    tempPostInfo = TempPostInfo(
                        reduce.postId, state.exhibitName, state.categorySelect,
                        state.exhibitDate, state.exhibitLink.ifBlank { null })
                )
        }
    }
    enum class CreateType { CAMERA, ALBUM }
}
