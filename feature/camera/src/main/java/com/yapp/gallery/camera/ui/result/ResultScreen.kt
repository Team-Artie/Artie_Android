package com.yapp.gallery.camera.ui.result

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.yapp.gallery.common.theme.color_popUpBottom
import com.yapp.gallery.common.theme.color_white
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

// 카메라에서 사진 촬영 후 결과 화면
// 갤러리에서 사진 선택 후 결과 화면
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ResultRoute(
    popBackStack: () -> Unit,
    byteArray: ByteArray?,
    context: Activity
){
    val viewModel : ResultViewModel = resultViewModel(context = context, byteArray = byteArray)
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
        popBackStack = popBackStack
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ResultScreen(
    resultState: ResultState,
    modalBottomSheetState: ModalBottomSheetState,
    onClickRegister: () -> Unit,
    popBackStack: () -> Unit
){
    val pagerState = rememberPagerState()

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = "") },
        scrimColor = Color.Transparent,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
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
        }
    }
}

@Composable
fun resultViewModel(context : Activity, byteArray: ByteArray?) : ResultViewModel {
    val postId = context.intent.getLongExtra("postId", 0L)
    val imageList = context.intent.getStringArrayExtra("imageList")?.map {it.toUri()}?.toList() ?: emptyList()
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
            modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
            popBackStack = {},
            onClickRegister = {}
        )
    }
}