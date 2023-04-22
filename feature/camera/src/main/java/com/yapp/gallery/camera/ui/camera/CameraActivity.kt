package com.yapp.gallery.camera.ui.camera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.yapp.gallery.camera.navigation.CameraNavHost
import com.yapp.gallery.common.theme.GalleryTheme
import com.yapp.navigator.saver.SaverNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {

    @Inject
    lateinit var saverNavigator: SaverNavigator

//    private val permissionLaunch =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGrant ->
//            if (isGrant) {
//                binding.composeView.setContent {
//                    GalleryTheme {
//                        CameraView(
//                            onImageCapture = {
//                                val postId = intent.getLongExtra("postId", 0L)
//
//                                if (postId != 0L) {
//                                    startActivity(
//                                        saverNavigator.intentTo(this, it)
//                                            .putExtra("postId", postId)
//                                    )
//                                }
//                            },
//                            onDismiss = { finish() },
//                            outputDirectory = getFileOutput(),
//                            executor = cameraExecutor
//                        )
//                    }
//                }
//            } else {
//                // todo 퍼미션 거절 되었을 시 dialog 노출 후 화면 이탈 필요
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setStatusBarColor(color = Color.Transparent)
            }
            GalleryTheme {
                CameraNavHost(context = this)
            }
        }
    }
}
