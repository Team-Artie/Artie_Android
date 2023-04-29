package com.yapp.gallery.info.ui.info

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.info.navigation.ExhibitInfoNavHost
import com.yapp.gallery.navigation.home.HomeNavigator
import com.yapp.navigation.camera.CameraNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExhibitInfoActivity : ComponentActivity() {
    @Inject
    lateinit var cameraNavigator: CameraNavigator
    @Inject
    lateinit var homeNavigator: HomeNavigator

    private val exhibitId by lazy {
        intent.getLongExtra("exhibitId", 1)
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
                    cameraNavigator = cameraNavigator, homeNavigator = homeNavigator,
                    context = this
                )
            }
        }
    }
}
