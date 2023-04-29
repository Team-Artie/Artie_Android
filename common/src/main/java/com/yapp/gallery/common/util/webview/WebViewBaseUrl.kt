package com.yapp.gallery.common.util.webview

import com.yapp.gallery.common.BuildConfig

fun getWebViewBaseUrl() : String{
    return if (BuildConfig.DEBUG){
        BuildConfig.WEB_BASE_URL_DEV
    } else {
        BuildConfig.WEB_BASE_URL
    }
}