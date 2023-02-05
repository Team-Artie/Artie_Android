package com.yapp.gallery.home.screen.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.yapp.gallery.common.theme.*
import com.yapp.gallery.common.widget.CategoryCreateDialog
import com.yapp.gallery.common.widget.CenterTopAppBar
import com.yapp.gallery.common.widget.ConfirmDialog
import com.yapp.gallery.home.R
import com.yapp.gallery.home.widget.DatePickerSheet
import com.yapp.gallery.home.widget.RecordMenuDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun ExhibitRecordScreen(
    navController: NavHostController,
    navigateToCamera: () -> Unit,
    navigateToGallery: () -> Unit,
    viewModel: ExhibitRecordViewModel = hiltViewModel(),
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val exhibitName = rememberSaveable { mutableStateOf("") }
    val exhibitDate = rememberSaveable { mutableStateOf("") }
    val exhibitLink = rememberSaveable { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }

    // 카테고리 생성 다이얼로그
    val categoryDialogShown = remember { mutableStateOf(false) }
    // 기록 방법 다이얼로그
    val recordMenuDialogShown = remember { mutableStateOf(false) }
    // 임시 저장 다이얼로그
    val tempPostDialogShown = remember { mutableStateOf(false) }

    // 카테고리 리스트
    val categorySelect = rememberSaveable {
        mutableStateOf(-1L)
    }

    // Bottom Sheet State
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // 스크롤 상태
    val scrollState = rememberScrollState()
    // ScaffoldState
    val scaffoldState = rememberScaffoldState()

    // 이어서 하기 여부
    val isContinuous = rememberSaveable { mutableStateOf(false) }
    // 스크린 상태
    LaunchedEffect(viewModel.recordScreenState){
        viewModel.recordScreenState.collectLatest {
            when(it){
                is ExhibitRecordState.Response -> {
                    tempPostDialogShown.value = true
                }
                is ExhibitRecordState.Continuous -> {
                    with(it.tempPostInfo){
                        exhibitName.value = this.name
                        exhibitDate.value = this.postDate
                        exhibitLink.value = this.postLink ?: ""
                        categorySelect.value = this.categoryId
                        isContinuous.value = true
                    }
                }
                is ExhibitRecordState.Delete -> {
                    scope.launch {
                        val res = scaffoldState.snackbarHostState.showSnackbar(
                            message = "임시 보관된 전시를 삭제하였습니다.",
                            actionLabel = "취소",
                            duration = SnackbarDuration.Short
                        )
                        when(res){
                            SnackbarResult.Dismissed -> {viewModel.undoDelete(false)}
                            SnackbarResult.ActionPerformed -> {viewModel.undoDelete(true)}
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // 임시 포스트 다이얼로그
    if (tempPostDialogShown.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.temp_post_title),
            subTitle = stringResource(id = R.string.temp_post_guide),
            onDismissRequest = {
                viewModel.setContinuousDelete(false)
                tempPostDialogShown.value = false
            },
            onConfirm = {
                viewModel.setContinuousDelete(true)
                tempPostDialogShown.value = false},
            important = true
        )
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            DatePickerSheet(onDateSet = {
                exhibitDate.value = it
                scope.launch {
                    modalBottomSheetState.hide()
                }
            })
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
                    navigationIcon = if (navController.previousBackStackEntry != null) {
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    } else {
                        null
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
                    Spacer(modifier = Modifier.height(48.dp))
                    // 전시명 입력 부분
                    Column {
                        Text(
                            text = stringResource(id = R.string.exhibit_name),
                            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        BasicTextField(
                            maxLines = 1,
                            value = exhibitName.value,
                            onValueChange = { if (it.length <= 20) exhibitName.value = it },
                            textStyle = MaterialTheme.typography.h3,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                ) {
                                    if (exhibitName.value.isEmpty()) {
                                        Text(
                                            text = stringResource(id = R.string.exhibit_name_hint),
                                            style = MaterialTheme.typography.h3.copy(color = color_gray700)
                                        )
                                    }
                                }
                                innerTextField()
                            },
                            modifier = Modifier.onKeyEvent { keyEvent ->
                                if (keyEvent.key != Key.Enter || keyEvent.key != Key.SystemNavigationDown) return@onKeyEvent false
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                true
                            },
                            cursorBrush = SolidColor(color_white)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            color = color_gray600,
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.8.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                    // 전시 카테고리 선택 부분
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.exhibit_category), style =
                                MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (viewModel.categoryList.size.compareTo(5) == -1) {
                                CompositionLocalProvider(
                                    LocalMinimumTouchTargetEnforcement provides false,
                                ) {
                                    TextButton(
                                        onClick = {
                                            categoryDialogShown.value = !categoryDialogShown.value
                                            focusManager.clearFocus()
                                        },
                                        contentPadding = PaddingValues()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = color_mainGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "카테고리 만들기", style = MaterialTheme.typography.h4.copy(
                                                fontWeight = FontWeight.Medium, color = color_mainGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        if (viewModel.categoryList.isEmpty()) {
                            Spacer(modifier = Modifier.height(22.dp))
                            Text(
                                text = stringResource(id = R.string.category_empty_guide),
                                style = MaterialTheme.typography.h4.copy(color = color_gray700, lineHeight = 21.sp),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                            Spacer(modifier = Modifier.height(55.dp))
                        } else {
                            FlowRow{
                                viewModel.categoryList.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(71.dp),
                                        onClick = {
                                            if (categorySelect.value == item.id) categorySelect.value =
                                                -1
                                            else categorySelect.value = item.id
                                        },
                                        color = if (categorySelect.value == item.id) MaterialTheme.colors.secondary
                                        else MaterialTheme.colors.background,
                                        border = BorderStroke(
                                            1.dp,
                                            color = MaterialTheme.colors.secondary
                                        )

                                    ) {
                                        Text(
                                            text = item.name, style = MaterialTheme.typography.h4.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (categorySelect.value == item.id) Color(
                                                    0xFF282828
                                                )
                                                else MaterialTheme.colors.secondary
                                            ),
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(50.dp))
                        }

                    }
                    // 관람 날짜 고르기
                    Column {
                        Text(
                            text = stringResource(id = R.string.exhibit_date),
                            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .clickable(
                                    // ripple color 없애기
                                    indication = null,
                                    interactionSource = interactionSource
                                ) {
                                    scope.launch {
                                        focusManager.clearFocus()
                                        modalBottomSheetState.show()
                                    }
                                }
                        ) {
                            if (exhibitDate.value.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.exhibit_date_hint),
                                    fontFamily = pretendard,
                                    color = color_gray700,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Text(
                                    text = exhibitDate.value, fontFamily = pretendard, fontSize = 16.sp,
                                    maxLines = 1, modifier = Modifier.weight(1f),
                                    color = color_white
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            color = color_gray600,
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.8.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                    // 웹 링크
                    Column {
                        Text(
                            text = stringResource(id = R.string.exhibit_link),
                            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        BasicTextField(
                            maxLines = 1,
                            value = exhibitLink.value,
                            onValueChange = { exhibitLink.value = it },
                            textStyle = MaterialTheme.typography.h3,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    // Todo : 링크 임베드
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                ) {
                                    if (exhibitLink.value.isEmpty()) {
                                        Text(
                                            text = stringResource(id = R.string.exhibit_link_hint),
                                            style = MaterialTheme.typography.h3.copy(color = color_gray700)
                                        )
                                    }
                                }
                                innerTextField()
                            },
                            modifier = Modifier.onKeyEvent { keyEvent ->
                                if (keyEvent.key != Key.Enter || keyEvent.key != Key.SystemNavigationDown) return@onKeyEvent false
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                // Todo : 링크 임베드
                                true
                            },
                            cursorBrush = SolidColor(color_white)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            color = color_gray600,
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.8.dp
                        )
                    }
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
                    onClick = {
                        recordMenuDialogShown.value = true
                    },
                    enabled = exhibitName.value.isNotEmpty() && categorySelect.value != -1L && exhibitDate.value.isNotEmpty()
                ) {
                    Text(
                        text = if (isContinuous.value) stringResource(id = R.string.exhibit_crate_continuous_btn)
                        else stringResource(id = R.string.exhibit_create_btn),
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        fontFamily = pretendard
                    )
                }
            }

        }
        // 카테고리 다이얼로그
        if (categoryDialogShown.value) {
            CategoryCreateDialog(
                onCreateCategory = {
                    viewModel.addCategory(it)
                    categoryDialogShown.value = false
                },
                onDismissRequest = { categoryDialogShown.value = false },
                categoryState = viewModel.categoryState.collectAsState().value,
                checkCategory = { viewModel.checkCategory(it) }
            )
        }

        // 전시 기록 시작 다이얼로그
        if (recordMenuDialogShown.value) {
            RecordMenuDialog(
                onCameraClick = {
                    navigateToCamera()
                    recordMenuDialogShown.value = false },
                onGalleryClick = {
                    viewModel.createTempPost(1, exhibitName.value, categorySelect.value,
                        exhibitDate.value, exhibitLink.value.ifEmpty { null })
                    navigateToGallery()
                    recordMenuDialogShown.value = false },
                onDismissRequest = { recordMenuDialogShown.value = false }
            )
        }
    }

}

fun changeDateFormat(postDate: String): String {
    var dateList = postDate.split('/')
    return String.format(
        "%4d-%02d-%02d",
        dateList[0].toInt(),
        dateList[1].toInt(),
        dateList[2].toInt()
    )
}