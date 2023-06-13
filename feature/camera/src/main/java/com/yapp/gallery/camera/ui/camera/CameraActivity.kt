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
import com.yapp.gallery.common.theme.ArtieTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    private val resultFlow: MutableSharedFlow<List<String>> = MutableSharedFlow()

    private val imageCount by lazy {
        if (intent.hasExtra("count")) intent.getIntExtra("count", 0) else 0
    }

    private val imagePicker = registerImagePicker {imageList ->
        lifecycleScope.launch {
            resultFlow.emit(imageList.map { it.uri.toString() })
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
                            maxSize = MAX_IMAGE_COUNT - imageCount,
                            doneTitle = "완료",
                            limitMessage = "사진은 최대 ${MAX_IMAGE_COUNT - imageCount}장까지 선택 가능해요!"
                        )
                    )
                })
            }
        }
    }

    companion object{
        // Todo : 최대 장수 변경 할건지?!
        private const val MAX_IMAGE_COUNT = 5
    }
}

