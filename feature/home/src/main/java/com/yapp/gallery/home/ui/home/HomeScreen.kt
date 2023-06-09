package com.yapp.gallery.home.ui.home

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.util.webview.WebViewUtils
import com.yapp.gallery.common.util.webview.getWebViewBaseUrl
import com.yapp.gallery.common.util.webview.rememberWebView
import com.yapp.gallery.home.BuildConfig
import com.yapp.gallery.home.R
import com.yapp.gallery.home.ui.home.HomeContract.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    navigateToLogin: () -> Unit,
    navigateToRecord: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToInfo: (Long, String?) -> Unit,
    navigateToTest: (String?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    context: Activity,
){
    val webView by rememberWebView(onBridgeCalled = { action, payload ->
        viewModel.sendEvent(HomeEvent.OnWebViewClick(action, payload))
    })

    val webViewState = rememberSaveable() { Bundle() }

    LaunchedEffect(Unit){
        webViewState.getBundle("webViewState")?.let {
            webView.restoreState(it)
        }
    }

    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is HomeSideEffect.NavigateToLogin -> navigateToLogin()
                is HomeSideEffect.NavigateToRecord -> navigateToRecord()
                is HomeSideEffect.NavigateToProfile -> navigateToProfile()
                is HomeSideEffect.NavigateToInfo -> navigateToInfo(it.postId, it.idToken)
                is HomeSideEffect.SendRefreshToken -> {
//                    val jsonObject = JSONObject()
//
//                    val payloadObject = JSONObject()
//                    payloadObject.put("accessToken", it.idToken)
//
//                    val sendRefreshTokenObject = JSONObject()
//                    sendRefreshTokenObject.put("payload", payloadObject)
//
//                    jsonObject.put("SEND_REFRESH_TOKEN", sendRefreshTokenObject)
//
//                    Timber.e(jsonObject.toString())

                    WebViewUtils.cookieManager.setCookie(webView.url, "accessToken=${it.idToken}")

                    webView.reload()
//                    webView.loadUrl("javascript:window.postMessage(${jsonObject})")
                }
            }
        }
    }

    val homeState : HomeState by viewModel.viewState.collectAsStateWithLifecycle()


    var backKeyPressedTime = 0L
    BackHandler(enabled = true) {
        if (webView.canGoBack()){
            webView.goBack()
        } else {
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                // 뒤로가기 두 번 누르면 종료
                context.finishAffinity()
            } else {
                backKeyPressedTime = System.currentTimeMillis()
                Toast.makeText(context, "뒤로 가기 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit){
        onDispose {
            val bundle = Bundle()
            webView.saveState(bundle)
            webViewState.putBundle("webViewState", bundle)
        }
    }
    
//    DisposableEffect(lifecycleOwner){
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                viewModel.sendEvent(HomeEvent.CheckToken)
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }

    HomeScreen(
        homeState = homeState,
        webView = webView,
        onReload = { viewModel.sendEvent(HomeEvent.OnLoadAgain) },
        navigateToTest = {
            navigateToTest(homeState.currentToken)
        },
        hasBundle = webViewState.getBundle("webViewState") != null
    )
}

@Composable
private fun HomeScreen(
    homeState: HomeState,
    webView: WebView,
    onReload: () -> Unit,
    navigateToTest: () -> Unit,
    hasBundle: Boolean = false,
){
    val baseUrl = getWebViewBaseUrl() + stringResource(id = R.string.home_section)

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding(),
        floatingActionButton = {
//            if (BuildConfig.DEBUG){
//                Button(onClick = navigateToTest) {
//                    Text(text = "테스트 화면으로 이동", style = MaterialTheme.typography.h3.copy(
//                        color = color_black, fontWeight = FontWeight.SemiBold
//                    ))
//                }
//            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (homeState.connected) {
                AndroidView(
                    factory = { webView },
                    update = {
                        if (hasBundle.not() && homeState.currentToken != null && homeState.previousToken == null){
                            WebViewUtils.cookieManager.setCookie(baseUrl, "accessToken=${homeState.currentToken}")
                            it.loadUrl(baseUrl)
                        }
                    }
                )
            } else {
                HomeDisconnectedScreen(onReload)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview(){
    ArtieTheme {
        HomeScreen(
            homeState = HomeState(),
            webView = WebView(LocalContext.current).apply {
                loadUrl(BuildConfig.WEB_BASE_URL + stringResource(id = R.string.home_section))
            },
            onReload = {},
            navigateToTest = {}
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeDisconnectedScreen(
    onReload: () -> Unit,
){
    Text(
        text = stringResource(id = R.string.home_network_error),
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
        onClick = onReload
    ) {
        Text(
            text = stringResource(id = R.string.home_network_reload_btn),
            style = MaterialTheme.typography.h3.copy(
                color = Color(0xFFA7C5F9), fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(
                horizontal = 24.dp, vertical = 12.dp
            )
        )
    }
}
