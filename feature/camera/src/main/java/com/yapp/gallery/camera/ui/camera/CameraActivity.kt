package com.yapp.gallery.camera.ui.camera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.yapp.gallery.camera.navigation.CameraNavHost
import com.yapp.gallery.camera.util.uriToByteArray
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.navigation.home.HomeNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    @Inject lateinit var homeNavigator: HomeNavigator

    private val resultFlow: MutableSharedFlow<List<ByteArray>> = MutableSharedFlow()

    private val imagePicker = registerImagePicker {imageList ->
        lifecycleScope.launch {
            resultFlow.emit(imageList.map { uriToByteArray(this@CameraActivity, it.uri)})
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setStatusBarColor(color = Color.Transparent)
            }
            ArtieTheme {
                CameraNavHost(context = this, resultFlow = resultFlow, onLaunchImagePicker = {
                    imagePicker.launch(
                        ImagePickerConfig(
                            isMultipleMode = true,
                            maxSize = 5,
                            doneTitle = "완료",
                            limitMessage = "사진은 최대 5장까지 선택 가능해요!"
                        )
                    )
                }, homeNavigator = homeNavigator)
            }
        }
    }
}

