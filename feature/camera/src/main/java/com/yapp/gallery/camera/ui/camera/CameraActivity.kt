package com.yapp.gallery.camera.ui.camera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.yapp.gallery.camera.navigation.CameraNavHost
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.navigation.camera.CameraNavigator
import com.yapp.navigator.saver.SaverNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
//    @Inject lateinit var cameraNavigator: CameraNavigator
//
//    private val imagePicker = registerImagePicker {imageList ->
//        if(imageList.isNotEmpty()) {
//            startActivity(
//                cameraNavigator.navigateWithUris(
//                    this,
//                    intent.getLongExtra("postId", 0L),
//                    imageList.map { it.uri }
//                )
//            )
//        }
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setStatusBarColor(color = Color.Transparent)
            }
            ArtieTheme {
                CameraNavHost(context = this)
            }
        }
    }
}
