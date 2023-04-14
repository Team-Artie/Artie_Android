package com.yapp.gallery.common.provider

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.yapp.gallery.common.util.WebViewUtils
import com.yapp.gallery.common.util.webview.NavigateJsObject
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class WebViewProvider @Inject constructor(
    @ActivityContext val context: Context
){
    inline fun getWebView(crossinline onBridgeCalled: (String, String?) -> Unit) : WebView{
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
                WebViewUtils.cookieManager.setAcceptCookie(true)
                WebViewUtils.cookieManager.setAcceptThirdPartyCookies(this@apply, true)
                javaScriptEnabled = true
                overScrollMode = WebView.OVER_SCROLL_NEVER
                javaScriptCanOpenWindowsAutomatically = true
            }
            addJavascriptInterface(NavigateJsObject { action, payload ->
                onBridgeCalled(action, payload)
            }, "android")
        }
    }
}