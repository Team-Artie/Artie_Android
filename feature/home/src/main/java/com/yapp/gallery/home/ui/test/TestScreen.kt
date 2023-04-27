package com.yapp.gallery.home.ui.test

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.yapp.gallery.common.provider.WebViewProvider
import com.yapp.gallery.home.BuildConfig

@Composable
fun TestRoute(
    token: String? = null,
    webViewProvider: WebViewProvider
){
    val webView = webViewProvider.getWebView { _, _ -> }

    TestScreen(webView = webView, token = token)
}

@Composable
private fun TestScreen(
    webView: WebView,
    token: String? = null,
){
    AndroidView(factory = {
        webView.apply {
            token?.let { t -> loadUrl(BuildConfig.WEB_BASE_URL + "/test/token", mapOf("Authorization" to t))}
        }
    })
}