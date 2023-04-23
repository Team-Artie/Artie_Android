package com.yapp.gallery.camera.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yapp.gallery.camera.ui.camera.CameraActivity
import com.yapp.navigation.camera.CameraNavigator
import javax.inject.Inject

class CameraNavigatorImpl @Inject constructor(): CameraNavigator {

    override fun navigate(context: Context): Intent {
        return Intent(context, CameraActivity::class.java)
    }

    override fun navigateWithUris(context: Context, postId: Long, uris: List<Uri>): Intent {
        return navigate(context).apply {
            putExtra("postId", postId)
            putExtra("imageList", uris.toTypedArray())
            putExtra("gallery", true)
        }
    }

}
