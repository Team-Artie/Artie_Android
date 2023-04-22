package com.yapp.gallery.home.ui.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.gallery.common.theme.*
import com.yapp.gallery.common.widget.CenterTopAppBar
import com.yapp.gallery.common.widget.ConfirmDialog
import com.yapp.gallery.home.R
import com.yapp.gallery.home.widget.DatePickerSheet
import com.yapp.gallery.home.widget.RecordMenuDialog
import com.yapp.gallery.home.widget.exhibit.ExhibitCategory
import com.yapp.gallery.home.widget.exhibit.ExhibitDate
import com.yapp.gallery.home.widget.exhibit.ExhibitLink
import com.yapp.gallery.home.widget.exhibit.ExhibitRecordName
import com.yapp.gallery.home.ui.record.ExhibitRecordContract.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExhibitRecordRoute(
    navigateToCamera: (Long) -> Unit,
    navigateToGallery: (Long) -> Unit,
    popBackStack: () -> Unit,
    viewModel: ExhibitRecordViewModel = hiltViewModel()
){
    val recordState : ExhibitRecordState by viewModel.viewState.collectAsStateWithLifecycle()

    // Bottom Sheet State
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // ScaffoldState
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is ExhibitRecordSideEffect.NavigateToCamera -> navigateToCamera(it.postId)
                is ExhibitRecordSideEffect.NavigateToGallery -> navigateToGallery(it.postId)
//                is ExhibitRecordSideEffect.NavigateToHome -> navigateToHome()
                is ExhibitRecordSideEffect.ShowSnackBar -> {
                    val res = scaffoldState.snackbarHostState.showSnackbar(
                        message = "임시 보관된 전시를 삭제하였습니다.",
                        actionLabel = "취소",
                        duration = SnackbarDuration.Short
                    )
                    when (res) {
                        SnackbarResult.ActionPerformed -> {
                            viewModel.sendEvent(ExhibitRecordEvent.OnDeleteCancel)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    // 임시 포스트 다이얼로그
    if (recordState.tempPostDialogShown) {
        ConfirmDialog(
            title = stringResource(id = R.string.temp_post_title),
            subTitle = stringResource(id = R.string.temp_post_guide),
            onDismissRequest = {
                viewModel.sendEvent(ExhibitRecordEvent.DeleteTempPost)
//                tempPostDialogShown.value = false
            },
            onConfirm = {
                viewModel.sendEvent(ExhibitRecordEvent.ContinueTempPost)
//                tempPostDialogShown.value = false
            },
            important = true
        )
    }

    // 전시 기록 시작 다이얼로그
    if (recordState.recordDialogShown) {
        RecordMenuDialog(
            onCameraClick = {
                viewModel.sendEvent(ExhibitRecordEvent.OnCameraClick)
            },
            onGalleryClick = {
                viewModel.sendEvent(ExhibitRecordEvent.OnGalleryClick)
            },
            onDismissRequest = { viewModel.sendEvent(ExhibitRecordEvent.OnCancelClick) }
        )
    }

    ExhibitRecordScreen(
        recordState = recordState,
        setExhibitDate = {
            viewModel.sendEvent(ExhibitRecordEvent.SetExhibitDate(it))
            scope.launch { modalBottomSheetState.hide() }
        },
        setExhibitName = { viewModel.sendEvent(ExhibitRecordEvent.SetExhibitName(it)) },
        setCategoryId = { viewModel.sendEvent(ExhibitRecordEvent.SetCategoryId(it)) },
        setExhibitLink = { viewModel.sendEvent(ExhibitRecordEvent.SetExhibitLink(it)) },
        addCategory = { viewModel.sendEvent(ExhibitRecordEvent.AddCategory(it)) },
        checkCategory = { viewModel.sendEvent(ExhibitRecordEvent.CheckCategory(it)) },
        onRecordClick = { viewModel.sendEvent(ExhibitRecordEvent.OnRecordClick) },
        popBackStack = popBackStack,
        scope = scope,
        scaffoldState = scaffoldState,
        modalBottomSheetState = modalBottomSheetState
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExhibitRecordScreen(
    recordState: ExhibitRecordState,
    addCategory: (String) -> Unit,
    checkCategory: (String) -> Unit,
    setExhibitDate: (String) -> Unit,
    setExhibitName: (String) -> Unit,
    setCategoryId: (Long) -> Unit,
    setExhibitLink: (String) -> Unit,
    onRecordClick: () -> Unit,
    popBackStack: () -> Unit,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    modalBottomSheetState : ModalBottomSheetState
){
    // 스크롤 상태
    val scrollState = rememberScrollState()

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))
            DatePickerSheet(onDateSet = setExhibitDate)
        },
        scrimColor = Color.Transparent,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = color_popUpBottom
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(it) { data ->
                    Snackbar(
                        snackbarData = data,
                        actionColor = color_purple500,
                        backgroundColor = Color.White,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            },
            topBar = {
                CenterTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    title = {
                        Text(
                            text = stringResource(id = R.string.exhibit_title),
                            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    ExhibitRecordContent(
                        setExhibitName = setExhibitName,
                        addCategory = addCategory,
                        checkCategory = checkCategory,
                        setCategoryId = setCategoryId,
                        setExhibitLink = setExhibitLink,
                        recordState = recordState,
                        modalBottomSheetState = modalBottomSheetState,
                        scope = scope
                    )
                }
                // 하단 버튼
                Button(
                    colors = ButtonDefaults.buttonColors(
                        disabledBackgroundColor = color_gray600,
                        disabledContentColor = color_gray900,
                        backgroundColor = color_mainGreen,
                        contentColor = color_black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 53.dp),
                    onClick = onRecordClick,
                    enabled = recordState.exhibitName.isNotEmpty() &&
                            recordState.categorySelect != -1L &&
                            recordState.exhibitDate.isNotEmpty()
                ) {
                    Text(
                        text = if (recordState.continuous) stringResource(id = R.string.exhibit_crate_continuous_btn)
                            else stringResource(id = R.string.exhibit_create_btn),
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        fontFamily = pretendard
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
private fun ExhibitRecordScreenPreview(){
    ArtieTheme {
        ExhibitRecordScreen(
            recordState = ExhibitRecordState(),
            addCategory = {},
            checkCategory = {},
            setExhibitDate = {},
            setExhibitName = {},
            setCategoryId = {},
            setExhibitLink = {},
            onRecordClick = { /*TODO*/ },
            popBackStack = { /*TODO*/ },
            scope = rememberCoroutineScope(),
            scaffoldState = rememberScaffoldState(),
            modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ExhibitRecordContent(
    setExhibitName: (String) -> Unit,
    addCategory: (String) -> Unit,
    checkCategory: (String) -> Unit,
    setCategoryId: (Long) -> Unit,
    setExhibitLink: (String) -> Unit,
    recordState: ExhibitRecordState,
    modalBottomSheetState : ModalBottomSheetState,
    scope: CoroutineScope,
    focusManager: FocusManager = LocalFocusManager.current,
) {
    val focusRequester = remember { FocusRequester() }

    Spacer(modifier = Modifier.height(48.dp))

    ExhibitRecordName(
        name = recordState.exhibitName,
        focusManager = focusManager,
        focusRequester = focusRequester,
        setExhibitName = setExhibitName
    )

    Spacer(modifier = Modifier.height(50.dp))

    ExhibitCategory(
        categoryList = recordState.categoryList,
        focusManager = focusManager,
        categorySelect = recordState.categorySelect,
        addCategory = addCategory,
        checkCategory = checkCategory,
        setCategoryId = setCategoryId,
        categoryState = recordState.categoryState
    )

    ExhibitDate(
        exhibitDate = recordState.exhibitDate,
        scope = scope,
        focusManager = focusManager,
        modalBottomSheetState = modalBottomSheetState
    )

    Spacer(modifier = Modifier.height(50.dp))

    ExhibitLink(
        exhibitLink = recordState.exhibitLink,
        setExhibitLink = setExhibitLink,
        focusManager = focusManager,
        focusRequester = focusRequester
    )
}