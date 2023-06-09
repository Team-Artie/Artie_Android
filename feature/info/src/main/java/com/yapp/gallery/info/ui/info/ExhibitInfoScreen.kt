package com.yapp.gallery.info.ui.info

import android.view.KeyEvent
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.util.webview.WebViewUtils
import com.yapp.gallery.common.util.webview.getWebViewBaseUrl
import com.yapp.gallery.common.util.webview.rememberWebView
import com.yapp.gallery.info.R
import com.yapp.gallery.info.ui.info.ExhibitInfoContract.*
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ExhibitInfoRoute(
    exhibitId: Long,
    navigateToCamera: (Long) -> Unit,
    navigateToGallery: (Long, Int) -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToWebPage: (String) -> Unit,
    popBackStack: () -> Unit,
    viewModel: ExhibitInfoViewModel = hiltViewModel(),
){
    val infoState : ExhibitInfoState by viewModel.viewState.collectAsStateWithLifecycle()

    // 웹뷰 정의
    val webView by rememberWebView(onBridgeCalled = { action, payload ->
        viewModel.sendEvent(ExhibitInfoEvent.OnWebViewClick(action, payload))
    }, options = {
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
    })

    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is ExhibitInfoSideEffect.NavigateToEdit -> {
                    navigateToEdit(it.data)
                }
                is ExhibitInfoSideEffect.NavigateToCamera -> {
                    navigateToCamera(it.exhibitId)
                }
                is ExhibitInfoSideEffect.NavigateToGallery -> {
                    navigateToGallery(it.exhibitId, it.count)
                }
                is ExhibitInfoSideEffect.PopBackStack -> {
                    popBackStack()
                }
                is ExhibitInfoSideEffect.ShowWebPage -> {
                    navigateToWebPage(it.url)
                }
                is ExhibitInfoSideEffect.SendRefreshToken -> {
                    WebViewUtils.cookieManager.setCookie(
                        webView.url, "accessToken=${it.idToken}"
                    )
                    webView.reload()
                }
            }
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
    val baseUrl = getWebViewBaseUrl() + stringResource(id = R.string.exhibit_info_section)

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .navigationBarsPadding()
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
                            WebViewUtils.cookieManager.setCookie(
                                baseUrl + exhibitId, "accessToken=${infoState.idToken}"
                            )

                            it.loadUrl(baseUrl + exhibitId)
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
        text = stringResource(id = R.string.info_network_error),
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
            text = stringResource(id = R.string.info_network_reload_btn),
            style = MaterialTheme.typography.h3.copy(
                color = Color(0xFFA7C5F9), fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(
                horizontal = 24.dp, vertical = 12.dp
            )
        )
    }
}

//@Composable
//fun infoViewModel(context: Activity, accessToken: String?) : ExhibitInfoViewModel {
//    val factory = EntryPointAccessors.fromActivity(
//        context,
//        InfoViewModelFactoryProvider::class.java
//    ).exhibitInfoViewModelFactory()
//
//    return viewModel(factory = ExhibitInfoViewModel.provideFactory(factory, accessToken))
//}