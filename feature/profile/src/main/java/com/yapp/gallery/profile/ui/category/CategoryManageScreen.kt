package com.yapp.gallery.profile.ui.category

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.common.theme.*
import com.yapp.gallery.common.widget.CategoryCreateDialog
import com.yapp.gallery.common.widget.CenterTopAppBar
import com.yapp.gallery.common.widget.ConfirmDialog
import com.yapp.gallery.domain.entity.category.PostContent
import com.yapp.gallery.domain.entity.home.CategoryItem
import com.yapp.gallery.profile.R
import com.yapp.gallery.profile.ui.category.CategoryManageContract.*
import com.yapp.gallery.profile.utils.DraggableItem
import com.yapp.gallery.profile.utils.rememberDragDropState
import com.yapp.gallery.profile.widget.CategoryEditDialog
import com.yapp.gallery.profile.widget.CustomSnackbarHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
internal fun CategoryManageRoute(
    popBackStack: () -> Unit,
    viewModel: CategoryManageViewModel = hiltViewModel(),
) {
    val categoryManageState: CategoryManageState by viewModel.viewState.collectAsStateWithLifecycle()
    val categoryState: BaseState<Boolean> by viewModel.categoryState.collectAsStateWithLifecycle()

    val categoryCreateDialogShown = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val snackState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collectLatest {
            when(it){
                is CategoryManageSideEffect.ShowSnackbar -> {
                    snackState.showSnackbar(
                        message = it.msg.asString(context), duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // 카테고리 생성 다이얼로그

    if (categoryCreateDialogShown.value) {
        CategoryCreateDialog(onCreateCategory = { viewModel.sendEvent(CategoryManageEvent.OnAddClick(it)) },
            onDismissRequest = { categoryCreateDialogShown.value = false },
            checkCategory = { viewModel.sendEvent(CategoryManageEvent.CheckAddable(it)) },
            categoryState = categoryState
        )
    }

    CategoryManageScreen(
        categoryManageState = categoryManageState,
        categoryState = categoryState,
        categoryCreateDialogShown = categoryCreateDialogShown,
        categoryList = categoryManageState.categoryList,
        onReorder = { from, to -> viewModel.sendEvent(CategoryManageEvent.OnReorderCategory(from, to)) },
        onEditCategory = { category, name -> viewModel.sendEvent(CategoryManageEvent.OnEditClick(category, name)) },
        onExpandCategory = { position -> viewModel.sendEvent(CategoryManageEvent.OnExpandClick(position)) },
        onCheckEditable = { origin, edited -> viewModel.sendEvent(CategoryManageEvent.CheckEditable(origin, edited)) },
        onDeleteCategory = { category -> viewModel.sendEvent(CategoryManageEvent.OnDeleteClick(category)) },
        snackState = snackState,
        scope = scope,
        popBackStack = popBackStack
    )
}

@Composable
private fun CategoryManageScreen(
    categoryManageState: CategoryManageState,
    categoryState: BaseState<Boolean>,
    categoryCreateDialogShown: MutableState<Boolean>,
    categoryList: List<CategoryItem>,
    onReorder: (Int, Int) -> Unit,
    onEditCategory : (CategoryItem, String) -> Unit,
    onExpandCategory : (Int) -> Unit,
    onCheckEditable : (String, String) -> Unit,
    onDeleteCategory : (CategoryItem) -> Unit,
    snackState : SnackbarHostState,
    scope: CoroutineScope,
    popBackStack: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        CenterTopAppBar(modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.background,
            elevation = 0.dp,
            title = {
                Text(
                    text = stringResource(id = R.string.category_manage_btn),
                    style = MaterialTheme.typography.h2.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = popBackStack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        if (categoryList.size < 5) categoryCreateDialogShown.value = true
                        else scope.launch {
                            snackState.showSnackbar(
                                message = "최대 5개까지 생성 가능해요!", duration = SnackbarDuration.Short
                            )
                        }
                    }, enabled = categoryManageState.isLoading.not()
                ) {
                    Text(
                        text = stringResource(id = R.string.category_add),
                        style = MaterialTheme.typography.h3.copy(
                            fontWeight = FontWeight.Medium
                        ),
                    )
                }
            })

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 카테고리 정보 뷰
            CategoryListView(
                categoryManageState = categoryManageState,
                categoryState = categoryState,
                categoryPostFlowList = categoryManageState.categoryPostFlowList,
                onReorder = { from, to -> onReorder(from, to) },
                onExpandCategory = onExpandCategory,
                onEditCategory = onEditCategory,
                onCheckEditable = onCheckEditable,
                onDeleteCategory =  onDeleteCategory,
                onCreateButtonClicked = { categoryCreateDialogShown.value = true },
                categoryList = categoryList,
                scope = scope
            )

            // 커스텀 Snackbar
            Column(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
            ) {
                CustomSnackbarHost(snackbarHostState = snackState)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryListView(
    categoryManageState: CategoryManageState,
    categoryPostFlowList: List<Flow<PagingData<PostContent>>>,
    categoryState: BaseState<Boolean>,
    onReorder : (Int, Int) -> Unit,
    onEditCategory : (CategoryItem, String) -> Unit,
    onExpandCategory : (Int) -> Unit,
    onCheckEditable : (String, String) -> Unit,
    onDeleteCategory : (CategoryItem) -> Unit,
    onCreateButtonClicked: () -> Unit,
    categoryList: List<CategoryItem>,
    scope: CoroutineScope,
    listState: LazyListState = rememberLazyListState(),
) {
    val dragDropState = rememberDragDropState(lazyListState = listState) { from, to -> onReorder(from, to) }

    var overscrollJob by remember { mutableStateOf<Job?>(null) }

    if (categoryManageState.categoryList.isNotEmpty()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.pointerInput(dragDropState) {
                detectDragGesturesAfterLongPress(onDrag = { change, offset ->
                    change.consume()
                    dragDropState.onDrag(offset = offset)

                    if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                    dragDropState.checkForOverScroll().takeIf { it != 0f }?.let {
                        overscrollJob = scope.launch {
                            dragDropState.state.animateScrollBy(
                                it * 2f, tween(easing = FastOutLinearInEasing)
                            )
                        }
                    } ?: run { overscrollJob?.cancel() }
                },
                    onDragStart = { offset -> dragDropState.onDragStart(offset) },
                    onDragEnd = {
                        dragDropState.onDragInterrupted()
                        overscrollJob?.cancel()
                    },
                    onDragCancel = {
                        dragDropState.onDragInterrupted()
                        overscrollJob?.cancel()
                    })
            }) {
            item { Spacer(modifier = Modifier.height(36.dp)) }

            itemsIndexed(
                items = categoryList,
                key = { _, item -> item.id }
            ) { index, item ->
                // Draggable Item
                DraggableItem(dragDropState = dragDropState, index = index) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 10.dp else 0.dp)
                    CategoryListTile(
                        category = item,
                        categoryState = categoryState,
                        categoryPostFlow = categoryPostFlowList[index],
                        onExpandCategory = onExpandCategory,
                        onEditCategory = onEditCategory,
                        onDeleteCategory = onDeleteCategory,
                        onCheckEditable = onCheckEditable,
                        isExpanded = categoryManageState.expandedList[index],
                        elevation = elevation,
                        index = index
                    )
                }
                // Divider
                if (index != categoryList.size - 1) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        color = color_gray700,
                        thickness = 0.4.dp
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (categoryManageState.isLoading) {
                // 로딩 중
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp), color = color_mainBlue
                )
            } else {
                // 카테고리 리스트가 빈 리스트인 경우
                CategoryEmptyView(onCreateButtonClicked = onCreateButtonClicked)
            }
        }
    }
}

@Composable
private fun CategoryListTile(
    category: CategoryItem,
    categoryState : BaseState<Boolean>,
    categoryPostFlow : Flow<PagingData<PostContent>>,
    onEditCategory : (CategoryItem, String) -> Unit,
    onExpandCategory : (Int) -> Unit,
    onCheckEditable : (String, String) -> Unit,
    onDeleteCategory : (CategoryItem) -> Unit,
    isExpanded: Boolean,
    elevation: Dp,
    index: Int
) {
    val categoryEditDialogShown = remember { mutableStateOf(false) }
    val categoryDeleteDialogShown = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(ambientColor = color_popUpBottom, elevation = elevation)
            .animateContentSize(animationSpec = tween())
            .clickable(onClick = { onExpandCategory(index) })
            .background(color = color_background)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // 카테고리 브리프 정보 및 첫 행
        ConstraintLayout(
            modifier = Modifier
                .padding(start = 20.dp)
                .fillMaxWidth()
        ) {
            val (button, row, text1, text2) = createRefs()
            IconButton(onClick = { onExpandCategory(index) },
                modifier = Modifier
                    .size(18.dp)
                    .constrainAs(button) {
                        start.linkTo(parent.start)
                        top.linkTo(text1.top)
                        bottom.linkTo(text1.bottom)
                    }
            ) {
                Icon(painter = if (isExpanded) painterResource(id = R.drawable.arrow_up)
                        else painterResource(id = R.drawable.arrow_down),
                    contentDescription = "categoryExpand",
                    tint = color_gray500,
                )
            }


            // 카테고리 이름
            Text(text = category.name,
                style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top)
                    start.linkTo(button.end, margin = 8.dp)
                    end.linkTo(row.start)
                    width = Dimension.fillToConstraints
                })


            // 편집 및 삭제
            Row(modifier = Modifier
                .constrainAs(row) {
                    start.linkTo(text1.end, margin = 12.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                    top.linkTo(text1.top)
                    bottom.linkTo(text1.bottom)
                }) {
                Text(text = stringResource(id = R.string.category_edit),
                    style = MaterialTheme.typography.h4.copy(color = color_gray500),
                    modifier = Modifier
                        .clickable {
                            onCheckEditable(category.name, category.name)
                            categoryEditDialogShown.value = true
                        }
                        .padding(8.dp))

                Text(text = stringResource(id = R.string.category_remove),
                    style = MaterialTheme.typography.h4.copy(color = color_gray500),
                    modifier = Modifier
                        .clickable { categoryDeleteDialogShown.value = true }
                        .padding(8.dp))
            }


            // 전시 기록 개수
            Text(
                text = "${category.postNum}${stringResource(id = R.string.category_exhibit_cnt)}",
                style = MaterialTheme.typography.h4.copy(color = color_gray500),
                modifier = Modifier.constrainAs(text2) {
                    start.linkTo(text1.start)
                    top.linkTo(text1.bottom, margin = 4.dp)
                },
                textAlign = TextAlign.Start
            )

        }

        val data = categoryPostFlow.collectAsLazyPagingItems()

        // 카테고리 전시 기록 리스트
        if (isExpanded) {
            CategoryPostPagingView(posts = data, modifier = Modifier.padding(start = 46.dp, end = 20.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 카테고리 삭제 다이얼로그
        if (categoryDeleteDialogShown.value) {
            ConfirmDialog(title = stringResource(id = R.string.category_delete_title),
                subTitle = stringResource(id = R.string.category_delete_guide),
                onDismissRequest = { categoryDeleteDialogShown.value = false },
                onConfirm = {
                    onDeleteCategory(category)
                    categoryDeleteDialogShown.value = false
                })
        }


        // 카테고리 편집 다이얼로그
        if (categoryEditDialogShown.value) {
            CategoryEditDialog(
                category = category.name,
                onEditCategory = { onEditCategory(category, it) },
                onDismissRequest = { categoryEditDialogShown.value = false },
                checkEditable = { it1, it2 -> onCheckEditable(it1, it2) },
                categoryState = categoryState
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CategoryEmptyView(
    onCreateButtonClicked: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.category_manage_empty_guide),
            style = MaterialTheme.typography.h3.copy(
                color = color_gray600,
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 카테고리 만들기 버튼
        Surface(shape = RoundedCornerShape(71.dp),
            color = MaterialTheme.colors.background,
            border = BorderStroke(1.dp, color = Color(0xFFA7C5F9)),
            onClick = onCreateButtonClicked
        ) {
            Text(
                text = stringResource(id = R.string.category_manage_create),
                style = MaterialTheme.typography.h3.copy(
                    color = Color(0xFFA7C5F9), fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(
                    horizontal = 24.dp, vertical = 12.dp
                )
            )
        }
    }
}

@Composable
private fun CategoryPostPagingView(
    posts : LazyPagingItems<PostContent>,
    modifier: Modifier = Modifier
){
    Spacer(modifier = Modifier.height(24.dp))
    when(posts.loadState.refresh){
        is LoadState.Loading -> {
            // 로딩 중
        }
        is LoadState.Error -> {
            // 에러 아이템
//            CategoryPostEmpty()
        }

        else -> {
            if (posts.itemCount == 0){
                CategoryPostEmpty()
            }
            else {
                LazyRow(
                    modifier = modifier
                ){
                    items(posts, key = { it.id}){post ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = post?.mainImage,
                                error = painterResource(id = R.drawable.bg_image_placeholder),
                                placeholder = painterResource(id = R.drawable.bg_image_placeholder),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(4.5.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            post?.name?.let {
                                Text(
                                    text = it, textAlign = TextAlign.Center, style = MaterialTheme.typography.h4.copy(
                                        fontWeight = FontWeight.Medium, color = color_gray300
                                    ), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier
                                        .width(100.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
    }
//
//        when (posts.loadState.append) { // Pagination
//            is LoadState.Error -> {
//                //state.error to get error message
//            }
//            is LoadState.Loading -> { // Pagination Loading UI
//                item {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center,
//                    ) {
//                        Text(text = "Pagination Loading")
//
//                        CircularProgressIndicator(color = Color.Black)
//                    }
//                }
//            }
//            else -> {}
//        }
    }
}

@Composable
private fun CategoryPostEmpty(){
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.category_exhibit_empty),
            style = MaterialTheme.typography.h3.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

