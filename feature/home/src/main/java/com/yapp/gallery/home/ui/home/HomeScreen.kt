package com.yapp.gallery.home.ui.home

import android.app.Activity
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yapp.gallery.common.provider.WebViewProvider
import com.yapp.gallery.home.ui.home.HomeContract.*
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.home.R
import com.yapp.gallery.home.provider.HomeViewModelFactoryProvider
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    navigateToRecord: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToCalendar: () -> Unit,
    navigateToInfo: (Long, String?) -> Unit,
    webViewProvider: WebViewProvider,
    context: Activity
){
    val viewModel = homeViewModel(context = context, accessToken = context.intent.getStringExtra("accessToken"))

    val homeState : HomeState by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is HomeSideEffect.NavigateToRecord -> navigateToRecord()
                is HomeSideEffect.NavigateToProfile -> navigateToProfile()
                is HomeSideEffect.NavigateToCalendar -> navigateToCalendar()
                is HomeSideEffect.NavigateToInfo -> navigateToInfo(it.postId, it.idToken)
            }
        }
    }

    val webView = webViewProvider.getWebView { action, payload ->
        viewModel.sendEvent(HomeEvent.OnWebViewClick(action, payload))
    }

    HomeScreen(
        homeState = homeState,
        webView = webView,
        onReload = { viewModel.sendEvent(HomeEvent.OnLoadAgain) }
    )
}

@Composable
private fun HomeScreen(
    homeState : HomeState,
    webView : WebView,
    onReload : () -> Unit
){
    val baseUrl = stringResource(id = R.string.home_base_url)
//    val baseUrl = "https://21st-all-rounder-team-2-web-bobeenlee.vercel.app/test-token"

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (homeState is HomeState.Disconnected){
                HomeDisconnectedScreen(onReload)
            } else {
                AndroidView(
                    factory = { webView },
                    update = {
                        if (homeState is HomeState.Connected) {
                            it.loadUrl(baseUrl, mapOf("Authorization" to homeState.idToken))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeDisconnectedScreen(
    onReload : () -> Unit
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

@Composable
fun homeViewModel(context: Activity, accessToken: String?) : HomeViewModel {
    val factory = EntryPointAccessors.fromActivity(
        context,
        HomeViewModelFactoryProvider::class.java
    ).homeViewModelFactory()

    return viewModel(factory = HomeViewModel.provideFactory(factory, accessToken))
}
