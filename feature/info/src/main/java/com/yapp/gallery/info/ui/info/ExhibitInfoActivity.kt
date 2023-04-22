package com.yapp.gallery.info.ui.info

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker
import com.yapp.gallery.common.provider.WebViewProvider
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.info.navigation.ExhibitInfoNavHost
import com.yapp.gallery.navigation.home.HomeNavigator
import com.yapp.navigation.camera.CameraNavigator
import com.yapp.navigator.saver.SaverNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExhibitInfoActivity : ComponentActivity() {
    @Inject
    lateinit var cameraNavigator: CameraNavigator
    @Inject
    lateinit var homeNavigator: HomeNavigator
    @Inject
    lateinit var saverNavigator: SaverNavigator

    @Inject
    lateinit var webViewProvider: WebViewProvider

    private val exhibitId by lazy {
        intent.getLongExtra("exhibitId", 1)
    }

    private val imagePicker = registerImagePicker {
        if(it.isNotEmpty()) {
            startActivity(saverNavigator.intentTo(context = this, uris = it.map { image -> image.uri }))
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
                ExhibitInfoNavHost(
                    exhibitId = exhibitId,
                    webViewProvider = webViewProvider,
                    cameraNavigator = cameraNavigator, homeNavigator = homeNavigator,
                    navToImgPicker = {
                        imagePicker.launch(
                            ImagePickerConfig(
                                isMultipleMode = true,
                                maxSize = 5,
                                doneTitle = "완료",
                                limitMessage = "사진은 최대 5장까지 선택 가능해요!"
                            )
                        )
                    },
                    context = this
                )
            }
        }
    }
}
