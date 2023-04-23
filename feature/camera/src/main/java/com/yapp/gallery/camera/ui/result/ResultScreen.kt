package com.yapp.gallery.camera.ui.result

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yapp.gallery.camera.R
import com.yapp.gallery.camera.model.ImageData
import com.yapp.gallery.camera.provider.ResultViewModelFactoryProvider
import com.yapp.gallery.camera.ui.result.ResultContract.*
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.common.theme.color_background
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.theme.color_gray700
import com.yapp.gallery.common.theme.color_gray900
import com.yapp.gallery.common.theme.color_mainGreen
import com.yapp.gallery.common.theme.color_popUpBottom
import com.yapp.gallery.common.theme.color_white
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
    popBackStack: (Boolean) -> Unit,
    byteArray: ByteArray?,
    uriList: List<Uri>?,
    context: Activity
){
    val viewModel : ResultViewModel = resultViewModel(context = context, byteArray = byteArray, uriList = uriList)
    val resultState : ResultState by viewModel.viewState.collectAsStateWithLifecycle()

    // Bottom Sheet State
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)


    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is ResultSideEffect.ShowBottomSheet -> {
                    modalBottomSheetState.show()
                }
            }
        }
    }

    ResultScreen(
        resultState = resultState,
        modalBottomSheetState = modalBottomSheetState,
        onClickRegister = { viewModel.sendEvent(ResultEvent.OnClickRegister) },
        setAuthorName = { viewModel.sendEvent(ResultEvent.SetAuthorName(it)) },
        setPostName = { viewModel.sendEvent(ResultEvent.SetPostName(it)) },
        setTempTag = { viewModel.sendEvent(ResultEvent.SetTempTag(it)) },
        popBackStack = {
            popBackStack(resultState.imageList.isNotEmpty())
        }
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ResultScreen(
    resultState: ResultState,
    modalBottomSheetState: ModalBottomSheetState,
    onClickRegister: () -> Unit,
    setAuthorName: (String) -> Unit,
    setPostName: (String) -> Unit,
    setTempTag: (String) -> Unit,
    popBackStack: () -> Unit,
    scope: CoroutineScope = rememberCoroutineScope()
){
    val pagerState = rememberPagerState()

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))
            ResultRegisterBottomSheet(
                resultState = resultState,
                setAuthorName = setAuthorName,
                setPostName = setPostName,
                setTempTag = setTempTag,
                onSkip = { /*TODO*/ },
                onRegister = { /*TODO*/ }) },
        scrimColor = Color.Transparent,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        BackHandler(enabled = modalBottomSheetState.isVisible) {
            scope.launch {
                modalBottomSheetState.hide()
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .navigationBarsPadding()
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
            else if (resultState.captureData != null){
                Image(
                    painter = rememberAsyncImagePainter(resultState.captureData.byteArray),
                    contentDescription = "image",
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
                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 47.dp, start = 16.dp, bottom = 14.dp)
                        .size(24.dp),
                    onClick = popBackStack
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
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

            if (modalBottomSheetState.isAnimationRunning || modalBottomSheetState.isVisible){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = color_background.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ResultRegisterBottomSheet(
    resultState: ResultState,
    setAuthorName: (String) -> Unit,
    setPostName: (String) -> Unit,
    setTempTag: (String) -> Unit,
    onSkip: () -> Unit,
    onRegister: () -> Unit,
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
            focusManager = focusManger
        )
        Spacer(modifier = Modifier.height(50.dp))


        ResultRegisterField(
            title = stringResource(id = R.string.post_name),
            hint = stringResource(id = R.string.post_name_hint),
            value = resultState.postName,
            onDataSet = setPostName,
            focusRequester = focusRequester,
            focusManager = focusManger
        )
        Spacer(modifier = Modifier.height(50.dp))


        ResultRegisterField(
            title = stringResource(id = R.string.emotional_tag),
            hint = stringResource(id = R.string.emotional_tag_hint),
            value = resultState.tempTag,
            onDataSet = setTempTag,
            focusRequester = focusRequester,
            focusManager = focusManger
        )

        Box(modifier = Modifier.height(112.dp)){

        }


    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ResultRegisterField(
    title: String,
    hint: String,
    value: String,
    onDataSet: (String) -> Unit,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
){
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)) {
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
                    focusManager.clearFocus()
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
            onSkip = {},
            onRegister = {}
        )
    }
}

@Composable
fun resultViewModel(context : Activity, byteArray: ByteArray?, uriList: List<Uri>?) : ResultViewModel {
    val postId = context.intent.getLongExtra("postId", 0L)

//    val imageList = if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        context.intent.getParcelableArrayExtra("imageList", Uri::class.java)?.toList() ?: emptyList()
//    } else {
//        context.intent.getParcelableArrayExtra("imageList")?.toList() ?: emptyList()
//    }

    val factory = EntryPointAccessors.fromActivity(
        context,
        ResultViewModelFactoryProvider::class.java
    ).resultViewModelFactory()

    Timber.e("postId : $postId, imageList : $uriList")
    return viewModel(factory = ResultViewModel.provideFactory(factory, postId, byteArray, uriList ?: emptyList()))
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview(){
    ArtieTheme {
        ResultScreen(
            resultState = ResultState(),
            modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
            popBackStack = {},
            setAuthorName = {},
            setPostName = {},
            setTempTag = {},
            onClickRegister = {}
        )
    }
}