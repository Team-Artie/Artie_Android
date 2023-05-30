package com.yapp.gallery.profile.ui.category

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.yapp.gallery.common.base.BaseStateViewModel
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.common.model.UiText
import com.yapp.gallery.domain.entity.home.CategoryItem
import com.yapp.gallery.domain.usecase.category.DeleteCategoryUseCase
import com.yapp.gallery.domain.usecase.category.EditCategorySequenceUseCase
import com.yapp.gallery.domain.usecase.category.EditCategoryUseCase
import com.yapp.gallery.domain.usecase.category.GetCategoryPostUseCase
import com.yapp.gallery.domain.usecase.record.CreateCategoryUseCase
import com.yapp.gallery.domain.usecase.record.GetCategoryListUseCase
import com.yapp.gallery.profile.R
import com.yapp.gallery.profile.ui.category.CategoryManageContract.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val getCategoryListUseCase: GetCategoryListUseCase,
    private val editCategoryUseCase: EditCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val changeSequenceUseCase: EditCategorySequenceUseCase,
    private val getCategoryPostUseCase: GetCategoryPostUseCase
) : BaseStateViewModel<CategoryManageState, CategoryManageEvent, CategoryManageReduce, CategoryManageSideEffect>(CategoryManageState()) {
    // 카테고리 자체 상태
    private var _categoryState = MutableStateFlow<BaseState<Boolean>>(BaseState.None)
    val categoryState : StateFlow<BaseState<Boolean>>
        get() = _categoryState


    init {
        getCategoryList()
    }

    private fun getCategoryList(){
        getCategoryListUseCase()
            .catch {
                sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_load_error)))
                updateState(CategoryManageReduce.CategoryListLoadError(it.message.toString()))
            }
            .onEach {
                // 각 카테고리 상태 추가
                updateState(CategoryManageReduce.CategoryListLoaded(it))
                val flowList = it.map { category ->
                    getCategoryPostUseCase(category.id).cachedIn(viewModelScope)
                }
                updateState(CategoryManageReduce.CategoryPostFlowListLoaded(flowList))
            }
            .launchIn(viewModelScope)
    }
    private fun checkCategory(category: String){
        if (viewState.value.categoryList.find { it.name == category } != null){
            _categoryState.value = BaseState.Error("이미 존재하는 카테고리입니다.")
        } else if (category.length > 20)
            _categoryState.value = BaseState.Error("카테고리는 20자 이하이어야 합니다.")
        else
            _categoryState.value = BaseState.Success(category.isNotEmpty())
    }

    private fun editCategory(category: CategoryItem, editedName: String){
        viewModelScope.launch {
            editCategoryUseCase(category.id, editedName)
                .catch {
                    Timber.tag("category error").e(it.message.toString())
                    sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_edit_error)))
                }
                .collectLatest {
                    Timber.tag("카테고리 편집").e(it.toString())
                    if (it)
                        updateState(CategoryManageReduce.UpdateCategoryItem(category, editedName))
                    else sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_edit_error)))
                }
        }
    }
    private fun deleteCategory(category : CategoryItem){
        deleteCategoryUseCase(category.id)
            .catch {
                sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_delete_erorr)))
            }
            .onEach {
                if (it){
                    updateState(CategoryManageReduce.DeleteCategoryItem(category))
                } else
                    sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_delete_erorr)))
            }
            .launchIn(viewModelScope)
    }

    private fun createCategory(categoryName: String){
        createCategoryUseCase(categoryName)
            .catch {
                sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_add_error)))
            }
            .onEach {
                // Todo : 카테고리 추가시 카테고리 아이템 추가
                updateState(
                    CategoryManageReduce.AddCategoryItem(
                        CategoryItem(it, categoryName, viewState.value.categoryList.size, 0),
                        getCategoryPostUseCase(it).cachedIn(viewModelScope)
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    private fun checkEditable(originCategory: String, category: String) {
        if (category == originCategory)
            _categoryState.value = BaseState.None
        else if (viewState.value.categoryList.find { it.name == category } != null)
            _categoryState.value = BaseState.Error("이미 존재하는 카테고리입니다.")
        else if (category.length > 20)
            _categoryState.value = BaseState.Error("카테고리는 20자 이하이어야 합니다.")
        else
            _categoryState.value = BaseState.Success(category.isNotEmpty())
    }

    private fun reorderItem(from: Int, to: Int){
        // 달라진게 있으면 재정렬
        val indices = viewState.value.categoryList.indices
        if (from != to && from in indices && to in indices){
            val tempList = viewState.value.categoryList.toMutableList().apply {
                this[from].sequence = this[to].sequence.also {
                    this[to].sequence = this[from].sequence
                }
                this.add(from, this.removeAt(to))
            }
            updateState(CategoryManageReduce.ChangeCategoryOrder(from, to, tempList))

//            changeSequenceUseCase(tempList)
//                .catch {
//                    sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_swap_error)))
//                    updateState(CategoryManageReduce.ChangeCategoryOrder(to, from, originList))
//                }
//                .onEach { isSuccessful ->
//                    if (isSuccessful.not()){
//                        updateState(CategoryManageReduce.ChangeCategoryOrder(to, from, originList))
//                        sendSideEffect(CategoryManageSideEffect.ShowSnackbar(UiText.StringResource(R.string.category_swap_error)))
//                    }
//                }.launchIn(viewModelScope)
        }
    }

    private fun changeSequence(){
        if (currentState.categoryList != currentState.originList){
            CoroutineScope(Dispatchers.IO).launch {
                changeSequenceUseCase(currentState.categoryList)
                    .collect{
                        this.cancel()
                    }
            }
        }
    }

//    // 카테고리 별 세부 전시 조회
//    private fun expandCategory(index: Int){
//        when (_categoryPostStateList[index]){
//            is CategoryPostState.Initial -> {
//                _categoryPostStateList[index] = CategoryPostState.Expanded(
//                    (_categoryPostStateList[index] as CategoryPostState.Initial).postFlow
//                )
//            }
//            // 단순 확장 Or not
//            else ->{
//                val postDetail = if (_categoryPostStateList[index] is CategoryPostState.Expanded)
//                    (_categoryPostStateList[index] as CategoryPostState.Expanded).postFlow
//                else (_categoryPostStateList[index] as CategoryPostState.UnExpanded).postFlow
//
//                _categoryPostStateList[index] = if (_categoryPostStateList[index] is CategoryPostState.Expanded)
//                    CategoryPostState.UnExpanded(postDetail)
//                else CategoryPostState.Expanded(postDetail)
//            }
//        }
//    }

//    private fun pagingLoadError(index: Int){
//        viewModelScope.launch {
//            _errorChannel.send(UiText.StringResource(R.string.network_error))
////            _categoryPostStateList[index] = CategoryPostState.UnExpanded(
////                (_categoryPostStateList[index] as CategoryPostState.Expanded).postFlow
////            )
//        }
//    }


    override fun handleEvents(event: CategoryManageEvent) {
        when (event){
            is CategoryManageEvent.OnAddClick -> {
                createCategory(event.category)
            }
            is CategoryManageEvent.OnEditClick -> {
                editCategory(event.categoryItem, event.edited)
            }
            is CategoryManageEvent.OnDeleteClick -> {
                deleteCategory(event.categoryItem)
            }
            is CategoryManageEvent.OnExpandClick -> {
                updateState(CategoryManageReduce.UpdateCategoryExpanded(event.index))
            }
            is CategoryManageEvent.OnReorderCategory -> {
                reorderItem(event.from, event.to)
            }
            is CategoryManageEvent.CheckAddable -> {
                checkCategory(event.category)
            }
            is CategoryManageEvent.CheckEditable -> {
                checkEditable(event.origin, event.edited)
            }
            is CategoryManageEvent.OnExpandLoadError -> {
//                pagingLoadError(event.position)
            }
            is CategoryManageEvent.OnDispose -> {
                changeSequence()
            }
        }
    }

    override fun reduceState(
        state: CategoryManageState,
        reduce: CategoryManageReduce,
    ): CategoryManageState {
        return when (reduce) {
            is CategoryManageReduce.CategoryListLoaded ->
                state.copy(
                    isLoading = false,
                    categoryList = reduce.categoryList,
                    originList = reduce.categoryList,
                    expandedList = List(reduce.categoryList.size) { false },
                )
            is CategoryManageReduce.CategoryPostFlowListLoaded ->
                state.copy(
                    categoryPostFlowList = reduce.categoryPostFlowList
                )
            is CategoryManageReduce.UpdateCategoryItem ->
                state.copy(
                    categoryList = state.categoryList.toMutableList().apply {
                        this[this.indexOf(reduce.categoryItem)] = reduce.categoryItem.copy(name = reduce.edited) }
                )

            is CategoryManageReduce.CategoryListLoadError ->
                state.copy(
                    isLoading = false,
                )

            is CategoryManageReduce.AddCategoryItem -> {
                state.copy(
                    categoryList = state.categoryList.toMutableList().apply { add(reduce.categoryItem) },
                    categoryPostFlowList = state.categoryPostFlowList.toMutableList().apply { add(reduce.flow) },
                    expandedList = state.expandedList.toMutableList().apply { add(false) }
                )
            }

            is CategoryManageReduce.DeleteCategoryItem -> {
                val position = state.categoryList.indexOf(reduce.categoryItem)
                state.copy(
                    categoryList = state.categoryList.toMutableList().apply { removeAt(position) },
                    categoryPostFlowList = state.categoryPostFlowList.toMutableList().apply { removeAt(position) },
                    expandedList = state.expandedList.toMutableList().apply { removeAt(position) }
                )
            }

            is CategoryManageReduce.UpdateCategoryExpanded -> {
                state.copy(
                    expandedList = state.expandedList.toMutableList().apply {
                        this[reduce.index] = !this[reduce.index]
                    }
                )
            }

            is CategoryManageReduce.ChangeCategoryOrder ->{
                state.copy(
                    categoryList = reduce.updatedList,
                    categoryPostFlowList = state.categoryPostFlowList.toMutableList().apply {
                        this.add(reduce.from, this.removeAt(reduce.to))
                    },
                    expandedList = state.expandedList.toMutableList().apply {
                        this.add(reduce.from, this.removeAt(reduce.to))
                    }
                )
            }
        }
    }
}