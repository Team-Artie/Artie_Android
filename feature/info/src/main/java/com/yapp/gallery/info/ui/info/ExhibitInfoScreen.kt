package com.yapp.gallery.info.ui.info

import android.app.Activity
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yapp.gallery.common.provider.WebViewProvider
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.info.R
import com.yapp.gallery.info.provider.InfoViewModelFactoryProvider
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ExhibitInfoRoute(
    exhibitId: Long,
    webViewProvider: WebViewProvider,
    navigateToCamera: () -> Unit,
    navigateToGallery: () -> Unit,
    navigateToEdit: (String) -> Unit,
    popBackStack: () -> Unit,
    context: Activity,
){
    val viewModel = infoViewModel(context = context, accessToken = context.intent.getStringExtra("accessToken"))
    val infoState : ExhibitInfoState by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is ExhibitInfoSideEffect.NavigateToEdit -> {
                    navigateToEdit(it.data)
                }
                is ExhibitInfoSideEffect.NavigateToCamera -> {
                    navigateToCamera()
                }
                is ExhibitInfoSideEffect.NavigateToGallery -> {
                    navigateToGallery()
                }
                is ExhibitInfoSideEffect.PopBackStack -> {
                    popBackStack()
                }
            }
        }
    }

    // 전체 화면 및 상태바 투명화
    LaunchedEffect(Unit){
        context.window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    // 웹뷰 정의
    val webView = webViewProvider.getWebView{ action, payload ->
        viewModel.sendEvent(ExhibitInfoEvent.OnWebViewClick(action, payload))
    }.apply {
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                if (this.canGoBack()) {
                    this.goBack()
                } else {
                    popBackStack()
                }
            }
            return@setOnKeyListener true
        }
    }

    ExhibitInfoScreen(
        exhibitId = exhibitId,
        webView = webView,
        infoState = infoState,
        onReload = { viewModel.sendEvent(ExhibitInfoEvent.OnLoadAgain) }
    )
}

@Composable
private fun ExhibitInfoScreen(
    exhibitId : Long,
    webView: WebView,
    infoState: ExhibitInfoState,
    onReload: () -> Unit,
){
    val baseUrl = stringResource(id = R.string.exhibit_info_base_url)

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (infoState is ExhibitInfoState.Disconnected){
                ExhibitInfoDisconnectedScreen(onReload = onReload)
            }
            else {
                AndroidView(
                    factory = { webView },
                    update = {
                        if (infoState is ExhibitInfoState.Connected){
                            it.loadUrl(baseUrl + exhibitId, mapOf("Authorization" to infoState.idToken))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExhibitInfoDisconnectedScreen(
    onReload: () -> Unit,
){
    Text(
        text = stringResource(id = com.yapp.gallery.home.R.string.home_network_error),
        style = MaterialTheme.typography.h3.copy(
            color = color_gray600,
            lineHeight = 24.sp
        ),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))

    // 네트워크 재요청 버튼
    Surface(shape = RoundedCornerShape(71.dp),
        color = MaterialTheme.colors.background,
        border = BorderStroke(1.dp, color = Color(0xFFA7C5F9)),
        onClick = onReload ) {
        Text(
            text = stringResource(id = com.yapp.gallery.home.R.string.home_network_reload_btn),
            style = MaterialTheme.typography.h3.copy(
                color = Color(0xFFA7C5F9), fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(
                horizontal = 24.dp, vertical = 12.dp
            )
        )
    }
}

@Composable
fun infoViewModel(context: Activity, accessToken: String?) : ExhibitInfoViewModel {
    val factory = EntryPointAccessors.fromActivity(
        context,
        InfoViewModelFactoryProvider::class.java
    ).exhibitInfoViewModelFactory()

    return viewModel(factory = ExhibitInfoViewModel.provideFactory(factory, accessToken))
}