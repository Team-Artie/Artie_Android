package com.yapp.gallery.common.util.webview

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import timber.log.Timber

class WebViewProvider(
    private val context: Context,
    private val onBridgeCalled: (String, String?) -> Unit
) : Lazy<WebView>{
    private var cached: WebView? = null

    private fun getWebView() : WebView{
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewUtils.webViewClient
            webChromeClient = WebViewUtils.webChromeClient
            settings.run {
                setBackgroundColor(0)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                clearCache(false)
                clearHistory()
                WebViewUtils.cookieManager.acceptThirdPartyCookies(this@apply)
                WebViewUtils.cookieManager.apply {
                    setAcceptCookie(true)
                }
                javaScriptEnabled = true
                overScrollMode = WebView.OVER_SCROLL_NEVER
                javaScriptCanOpenWindowsAutomatically = true
            }
            addJavascriptInterface(NavigateJsObject { action, payload ->
                onBridgeCalled(action, payload)
            }, "android")
        }
    }

    override val value: WebView
        get() {
            val webView = cached
            Timber.e("cached: $cached")
            if (webView == null){
                getWebView().also {
                    cached = it
                    return it
                }
            } else {
                return webView
            }
        }

    override fun isInitialized(): Boolean = cached != null
}

@Composable
fun rememberWebView(
    onBridgeCalled: (String, String?) -> Unit,
    options : (WebView.() -> Unit) = {}
) : Lazy<WebView> {
    val context = LocalContext.current
    return remember{
        WebViewProvider(context, onBridgeCalled).apply {
            options(value)
        }
    }
}