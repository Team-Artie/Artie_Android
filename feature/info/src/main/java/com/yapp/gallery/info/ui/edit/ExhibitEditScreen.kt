package com.yapp.gallery.info.ui.edit

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.common.theme.color_black
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.theme.color_gray900
import com.yapp.gallery.common.theme.color_mainGreen
import com.yapp.gallery.common.theme.color_popUpBottom
import com.yapp.gallery.common.theme.color_purple500
import com.yapp.gallery.common.theme.pretendard
import com.yapp.gallery.common.widget.CenterTopAppBar
import com.yapp.gallery.common.widget.ConfirmDialog
import com.yapp.gallery.info.R
import com.yapp.gallery.record.widget.DatePickerSheet
import com.yapp.gallery.record.widget.exhibit.ExhibitCategory
import com.yapp.gallery.record.widget.exhibit.ExhibitDate
import com.yapp.gallery.record.widget.exhibit.ExhibitLink
import com.yapp.gallery.record.widget.exhibit.ExhibitRecordName
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ExhibitEditScreen(
    popBackStack: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: ExhibitEditViewModel = hiltViewModel(),
    context: Context = LocalContext.current
){
    // 키보드 포커스
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 카테고리 State
    val categoryState: BaseState<Boolean> by viewModel.categoryState.collectAsState()

    // Bottom Sheet State
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // 스크롤 상태
    val scrollState = rememberScrollState()
    // ScaffoldState
    val scaffoldState = rememberScaffoldState()

    val exhibitDeleteDialogShown = remember { mutableStateOf(false) }
    
    LaunchedEffect(viewModel.errors){
        viewModel.errors.collect{
            scaffoldState.snackbarHostState.showSnackbar(
                it.asString(context)
            )
        }
    }

    LaunchedEffect(viewModel.editState){
        viewModel.editState.collectLatest {
            when(it){
                is ExhibitEditState.Delete -> {
                    navigateToHome()
                }
                is ExhibitEditState.Update -> {
                    popBackStack()
                }
                is ExhibitEditState.Error -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        it.message.asString(context)
                    )
                }
                else -> {}
            }
        }
    }

    // 전시 정보 삭제 다이얼로그
    if (exhibitDeleteDialogShown.value){
        ConfirmDialog(
            title = stringResource(id = R.string.exhibit_delete_dialog_title),
            subTitle = stringResource(id = R.string.exhibit_delete_dialog_guide),
            onDismissRequest = { exhibitDeleteDialogShown.value = false },
            onConfirm = { viewModel.deleteRemotePost() }
        )
    }

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))
            DatePickerSheet(onDateSet = {
                viewModel.exhibitDate.value = it
                scope.launch {
                    modalBottomSheetState.hide()
                }
            })
        },
        scrimColor = Color.Transparent,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = color_popUpBottom,
        modifier = Modifier.navigationBarsPadding().statusBarsPadding()
    ){
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
                            text = stringResource(id = R.string.exhibit_edit_appbar_title),
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
                    },
                    actions = {
                        TextButton(
                            onClick = { exhibitDeleteDialogShown.value = true },
                        ) {
                            Text(
                                text = "전시 삭제",
                                style = MaterialTheme.typography.h3.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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

                    ExhibitRecordName(
                        name = viewModel.exhibitName.value,
                        focusManager = focusManager,
                        focusRequester = focusRequester,
                        setExhibitName = { viewModel.exhibitName.value = it }
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    ExhibitCategory(
                        categoryList = viewModel.categoryList,
                        focusManager = focusManager,
                        categorySelect = viewModel.categorySelect.value,
                        addCategory = { viewModel.addCategory(it)},
                        checkCategory = { viewModel.checkCategory(it)},
                        categoryState = categoryState,
                        setCategoryId = { viewModel.categorySelect.value = it },
                    )

                    ExhibitDate(
                        exhibitDate = viewModel.exhibitDate.value,
                        scope = scope,
                        focusManager = focusManager,
                        modalBottomSheetState = modalBottomSheetState
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    ExhibitLink(exhibitLink = viewModel.exhibitLink.value,
                        focusManager = focusManager,
                        focusRequester = focusRequester,
                        setExhibitLink = { viewModel.exhibitLink.value = it }
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
                    onClick = {
                        viewModel.updateRemotePost()
                    },
                    enabled = viewModel.exhibitName.value.isNotBlank() &&
                            viewModel.categorySelect.value != -1L &&
                            viewModel.exhibitDate.value.isNotEmpty()
                ) {
                    Text(
                        text = stringResource(id = R.string.exhibit_edit_update_btn),
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