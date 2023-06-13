package com.yapp.gallery.camera.ui.result

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.yapp.gallery.camera.R
import com.yapp.gallery.camera.provider.ResultViewModelFactoryProvider
import com.yapp.gallery.camera.ui.result.ResultContract.*
import com.yapp.gallery.camera.widget.EmotionalTag
import com.yapp.gallery.camera.widget.RegisterDialog
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.common.theme.color_background
import com.yapp.gallery.common.theme.color_black
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.theme.color_gray700
import com.yapp.gallery.common.theme.color_gray900
import com.yapp.gallery.common.theme.color_mainGreen
import com.yapp.gallery.common.theme.color_popUpBottom
import com.yapp.gallery.common.theme.color_white
import com.yapp.gallery.common.theme.pretendard
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

// 카메라에서 사진 촬영 후 결과 화면
// 갤러리에서 사진 선택 후 결과 화면
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ResultRoute(
    byteArray: ByteArray?,
    popBackStack: (Boolean) -> Unit,
    imageList: List<ByteArray>,
    context: Activity,
    navigateToInfo: (Long) -> Unit,
){
    val viewModel : ResultViewModel = resultViewModel(context = context, byteArray = byteArray, imageList = imageList)
    val resultState : ResultState by viewModel.viewState.collectAsStateWithLifecycle()

    // Bottom Sheet State
    val scaffoldBottomSheetState = rememberBottomSheetScaffoldState()


    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is ResultSideEffect.ShowBottomSheet -> {
                    scaffoldBottomSheetState.bottomSheetState.expand()
                }
                is ResultSideEffect.ShowToast -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is ResultSideEffect.NavigateToInfo -> {
                    Toast.makeText(context, "작품이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    navigateToInfo(it.postId)
                }
            }
        }
    }

    DisposableEffect(Unit){
        onDispose {
            byteArray?.let {
                context.cacheDir.listFiles { file ->
                    file.name.startsWith("temp.jpg")
                }?.forEach { file ->
                    file.deleteOnExit()
                }
            }
        }
    }


    // 업로드 다이얼로그
    if (resultState.registerDialogShown){
        RegisterDialog(
            onConfirm = { viewModel.sendEvent(ResultEvent.OnConfirmRegister) },
            onDismiss = { viewModel.sendEvent(ResultEvent.OnCancelRegister)},
            isContentEmpty = resultState.skip,
            registerState = resultState.registerState,
        )
    }

    ResultScreen(
        resultState = resultState,
        scaffoldState = scaffoldBottomSheetState,
        onClickRegister = { viewModel.sendEvent(ResultEvent.OnClickRegister) },
        onCaptureSave = { viewModel.sendEvent(ResultEvent.OnClickCaptureSave) },
        setAuthorName = { viewModel.sendEvent(ResultEvent.SetAuthorName(it)) },
        setPostName = { viewModel.sendEvent(ResultEvent.SetPostName(it)) },
        setTempTag = { viewModel.sendEvent(ResultEvent.SetTempTag(it)) },
        enterTag = { viewModel.sendEvent(ResultEvent.EnterTag) },
        onDeleteTag = { viewModel.sendEvent(ResultEvent.OnDeleteTag(it))},
        popBackStack = {
            popBackStack(resultState.imageList.isNotEmpty())
        },
        onRegister = { viewModel.sendEvent(ResultEvent.OnRegister(false))},
        onSkip = { viewModel.sendEvent(ResultEvent.OnRegister(true))},
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ResultScreen(
    resultState: ResultState,
    scaffoldState: BottomSheetScaffoldState,
    onClickRegister: () -> Unit,
    onCaptureSave: () -> Unit,
    setAuthorName: (String) -> Unit,
    setPostName: (String) -> Unit,
    setTempTag: (String) -> Unit,
    enterTag: () -> Unit,
    onDeleteTag: (String) -> Unit,
    onSkip: () -> Unit,
    onRegister: () -> Unit,
    popBackStack: () -> Unit,
    scope: CoroutineScope = rememberCoroutineScope()
){
    val pagerState = rememberPagerState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))
            ResultRegisterBottomSheet(
                resultState = resultState,
                setAuthorName = setAuthorName,
                setPostName = setPostName,
                setTempTag = setTempTag,
                enterTag = enterTag,
                onDeleteTag = onDeleteTag,
                onRegister = onRegister,
                onSkip = onSkip
            )
        },
        drawerScrimColor = Color.Transparent,
        sheetElevation = 0.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        BackHandler(enabled = scaffoldState.bottomSheetState.isExpanded) {
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }

        Box(modifier = Modifier
            .padding(it)
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
        ) {
            if (resultState.imageList.isNotEmpty()) {
                HorizontalPager(pageCount = resultState.imageList.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ){
                    Image(
                        painter = rememberAsyncImagePainter(resultState.imageList[it]),
                        contentDescription = "image",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(resultState.captureData?.byteArray).crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED).build(),
                    contentDescription ="image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = color_popUpBottom
                    ),
                    onClick = popBackStack,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (resultState.imageList.isEmpty()) stringResource(id = R.string.retry_camera_btn)
                        else stringResource(id = R.string.retry_gallery_btn),
                        style = MaterialTheme.typography.h3.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Button(
                    shape = RoundedCornerShape(24.dp),
                    onClick = onClickRegister,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.register_post_btn), style = MaterialTheme.typography.h3.copy(
                        fontWeight = FontWeight.SemiBold, color = color_popUpBottom))
                }

            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .alpha(0.7f)
                    .background(color_popUpBottom)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp, start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(24.dp) ,
                        onClick = popBackStack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    if (resultState.captureData != null && resultState.imageList.isEmpty()){
                        TextButton(onClick = onCaptureSave,
                        ) {
                            Text("저장", style = MaterialTheme.typography.h4.copy(
                                fontWeight = FontWeight.Medium, color = color_white))
                        }
                    }
                }




                if (resultState.imageList.size > 1) {
                    Text(text = "${pagerState.currentPage + 1} / ${resultState.imageList.size}",
                        style = MaterialTheme.typography.h2.copy(
                            fontWeight = FontWeight.SemiBold, color = color_white),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 47.dp, bottom = 14.dp)
                    )
                }
            }

            if (scaffoldState.bottomSheetState.isAnimationRunning || scaffoldState.bottomSheetState.isExpanded){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = color_background.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
private fun ResultRegisterBottomSheet(
    resultState: ResultState,
    setAuthorName: (String) -> Unit,
    setPostName: (String) -> Unit,
    setTempTag: (String) -> Unit,
    enterTag: () -> Unit,
    onDeleteTag: (String) -> Unit,
    onRegister: () -> Unit,
    onSkip: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    focusManger: FocusManager = LocalFocusManager.current
){
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = color_popUpBottom),
    ) {
        TextButton(onClick = onSkip,
            modifier = Modifier
                .align(Alignment.End)
                .padding(
                    top = 8.dp, end = 12.dp
                )
        ) {
            Text("넘어가기", style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Medium, color = color_gray600))
        }

        ResultRegisterField(
            title = stringResource(id = R.string.author_name),
            hint = stringResource(id = R.string.author_name_hint),
            value = resultState.authorName,
            onDataSet = setAuthorName,
            focusRequester = focusRequester,
            focusManager = focusManger,
            interactionSource = interactionSource,
        )
        Spacer(modifier = Modifier.height(50.dp))


        ResultRegisterField(
            title = stringResource(id = R.string.post_name),
            hint = stringResource(id = R.string.post_name_hint),
            value = resultState.postName,
            onDataSet = setPostName,
            focusRequester = focusRequester,
            focusManager = focusManger,
            interactionSource = interactionSource
        )
        Spacer(modifier = Modifier.height(50.dp))


        ResultRegisterField(
            title = stringResource(id = R.string.emotional_tag),
            hint = stringResource(id = R.string.emotional_tag_hint),
            value = resultState.tempTag,
            onDataSet = setTempTag,
            focusRequester = focusRequester,
            focusManager = focusManger,
            interactionSource = interactionSource,
            onEnterClick = enterTag,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .imePadding()
                .defaultMinSize(minHeight = 24.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ){
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ){
                items(resultState.tagList){
                    EmotionalTag(
                        tag = it,
                        onDelete = { onDeleteTag(it) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))

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
                .navigationBarsPadding()
                .padding(bottom = 53.dp),
            onClick = onRegister,
            enabled = resultState.authorName.isNotBlank() && resultState.postName.isNotBlank() && resultState.tagList.isNotEmpty()
        ) {
            Text(
                text = "완료",
                modifier = Modifier.padding(vertical = 12.dp),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                fontFamily = pretendard
            )
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ResultRegisterField(
    title: String,
    hint: String,
    value: String,
    onDataSet: (String) -> Unit,
    onEnterClick: (() -> Unit)? = null,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        BasicTextField(
            maxLines = 1,
            value = value,
            onValueChange = { if (it.length <= 20) onDataSet(it) },
            textStyle = MaterialTheme.typography.h3,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onEnterClick?.let { it() }?: run {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                }
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.h3.copy(color = color_gray700)
                        )
                    }
                }
                innerTextField()
            },
            modifier = Modifier
                .indicatorLine(
                    enabled = true,
                    isError = false,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = color_gray600,
                        unfocusedIndicatorColor = color_gray600,
                    ),
                    focusedIndicatorLineThickness = 0.8.dp,
                    unfocusedIndicatorLineThickness = 0.8.dp,
                    interactionSource = interactionSource,
                )
                .padding(bottom = 8.dp),
            cursorBrush = SolidColor(color_white)
        )

    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetPreview(){
    ArtieTheme {
        ResultRegisterBottomSheet(
            resultState = ResultState(),
            setAuthorName = {},
            setPostName = {},
            setTempTag = {},
            enterTag = {},
            onRegister = {},
            onDeleteTag = {},
            onSkip = {}
        )
    }
}

@Composable
fun resultViewModel(context : Activity, byteArray: ByteArray?, imageList: List<ByteArray>) : ResultViewModel {
    val postId = context.intent.getLongExtra("postId", 0L)

    val factory = EntryPointAccessors.fromActivity(
        context,
        ResultViewModelFactoryProvider::class.java
    ).resultViewModelFactory()

    Timber.e("postId : $postId, imageList : $imageList")
    return viewModel(factory = ResultViewModel.provideFactory(factory, postId, byteArray, imageList))
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview(){
    ArtieTheme {
        ResultScreen(
            resultState = ResultState(),
            scaffoldState = rememberBottomSheetScaffoldState(),
            popBackStack = {},
            setAuthorName = {},
            setPostName = {},
            setTempTag = {},
            enterTag = {},
            onDeleteTag = {},
            onClickRegister = {},
            onCaptureSave = {},
            onRegister = {},
            onSkip = {},
        )
    }
}