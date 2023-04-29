package com.yapp.gallery.home.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.home.navigation.HomeNavHost
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.navigation.camera.CameraNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @Inject lateinit var cameraNavigator: CameraNavigator
    @Inject lateinit var profileNavigator: ProfileNavigator
    @Inject lateinit var infoNavigator: ExhibitInfoNavigator

    private lateinit var navController : NavHostController

    private var backKeyPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            ArtieTheme {
                HomeNavHost(
                    navHostController = navController, profileNavigator = profileNavigator,
                    cameraNavigator = cameraNavigator, infoNavigator = infoNavigator,
                    context = this
                )
            }
        }
    }

    override fun onBackPressed() {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                // 뒤로가기 두 번 누르면 종료
                finishAffinity()
            } else {
                backKeyPressedTime = System.currentTimeMillis()
                Toast.makeText(this, "뒤로 가기 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}