package com.yapp.gallery.camera.util

import android.content.Context
import android.net.Uri

fun uriToByteArray(context: Context, uri: Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val byteArray = inputStream?.buffered()?.readBytes() ?: ByteArray(0)
    inputStream?.close()
    return byteArray
}