package com.yapp.gallery.profile

import com.yapp.gallery.domain.entity.home.CategoryItem
import com.yapp.gallery.domain.usecase.category.DeleteCategoryUseCase
import com.yapp.gallery.domain.usecase.category.EditCategorySequenceUseCase
import com.yapp.gallery.domain.usecase.category.EditCategoryUseCase
import com.yapp.gallery.domain.usecase.category.GetCategoryPostUseCase
import com.yapp.gallery.domain.usecase.record.CreateCategoryUseCase
import com.yapp.gallery.domain.usecase.record.GetCategoryListUseCase
import com.yapp.gallery.profile.ui.category.CategoryManageContract
import com.yapp.gallery.profile.ui.category.CategoryManageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CategoryManageViewModelTest {
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CategoryManageViewModel

    @Mock private lateinit var changeSequenceUseCase: EditCategorySequenceUseCase
    @Mock private lateinit var getCategoryListUseCase: GetCategoryListUseCase
    @Mock private lateinit var editCategoryUseCase: EditCategoryUseCase
    @Mock private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    @Mock private lateinit var createCategoryUseCase: CreateCategoryUseCase
    @Mock private lateinit var getCategoryPostUseCase: GetCategoryPostUseCase

    @Before
    fun setup() {
        viewModel = CategoryManageViewModel(
            getCategoryListUseCase,
            editCategoryUseCase,
            deleteCategoryUseCase,
            createCategoryUseCase,
            changeSequenceUseCase,
            getCategoryPostUseCase
        )

        viewModel.reduceTest(
            CategoryManageContract.CategoryManageReduce.CategoryListLoaded(
                listOf(
                    CategoryItem(1, "test", 0, 1),
                    CategoryItem(2, "test2", 1, 2),
                )
            )
        )

        viewModel.reduceTest(
            CategoryManageContract.CategoryManageReduce.CategoryPostFlowListLoaded(
                listOf(emptyFlow(), emptyFlow())
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `reducer 정상 작동 테스트`() = runTest {
        viewModel.reduceTest(
            CategoryManageContract.CategoryManageReduce.CategoryListLoaded(
                listOf(
                    CategoryItem(1, "test", 0, 1),
                    CategoryItem(2, "test2", 1, 2),
                )
            )
        )

        Assert.assertEquals(
            listOf(
                CategoryItem(1, "test", 0, 1),
                CategoryItem(2, "test2", 1, 2),
            ),
            viewModel.viewState.value.categoryList
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `전체 순서 변경 로직 테스트`() = runTest {
        // given
        val from = 0
        val to = 1
        val originalList = viewModel.viewState.value.categoryList
        // 단순히 toMutableList로 하면 원래 리스트 동일한 객체 참조
        val testList = originalList.map { it.copy() }.toMutableList().apply {
            this[from].sequence = this[to].sequence.also {
                this[to].sequence = this[from].sequence
            }
            this.add(from, this.removeAt(to))
        }

        // when
        Mockito.`when`(changeSequenceUseCase(testList)).thenReturn(flowOf(true))


        viewModel.handleEvents(CategoryManageContract.CategoryManageEvent.OnReorderCategory(from, to))
        // result
        Assert.assertEquals(
            listOf(
                CategoryItem(2, "test2", 0, 2),
                CategoryItem(1, "test", 1, 1),
            ),
            viewModel.viewState.value.categoryList
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `순서 변경 Reduce 테스트`() = runTest {
        val from = 0
        val to = 1

        val fromSequence = viewModel.viewState.value.categoryList[from].sequence
        val toSequence = viewModel.viewState.value.categoryList[to].sequence
        val tempList = viewModel.viewState.value.categoryList.toMutableList().apply {
            this[from].sequence = toSequence
            this[to].sequence = fromSequence
            this.add(from, this.removeAt(to))
        }

        viewModel.reduceTest(
            CategoryManageContract.CategoryManageReduce.ChangeCategoryOrder(from, to, tempList)
        )

        Assert.assertEquals(
            listOf(
                CategoryItem(2, "test2", 0, 2),
                CategoryItem(1, "test", 1, 1),
            ),
            viewModel.viewState.value.categoryList
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    class MainDispatcherRule(
        private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}