package com.yapp.gallery.home.ui.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.home.R
import com.yapp.gallery.home.navigation.HomeNavHost
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.login.LoginNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.gallery.navigation.record.RecordNavigator
import com.yapp.navigation.camera.CameraNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @Inject lateinit var loginNavigator: LoginNavigator
    @Inject lateinit var profileNavigator: ProfileNavigator
    @Inject lateinit var recordNavigator: RecordNavigator
    @Inject lateinit var infoNavigator: ExhibitInfoNavigator

    private lateinit var navController : NavHostController

    private val userExist by lazy {
        FirebaseAuth.getInstance().currentUser != null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            if (userExist){
                if(!intent.hasExtra("fromLogin")) delay(1000)
                window.setBackgroundDrawable(
                    ResourcesCompat.getDrawable(resources, R.color.background, null))
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberNavController()
            ArtieTheme {
                HomeNavHost(
                    navHostController = navController, loginNavigator = loginNavigator,
                    profileNavigator = profileNavigator, recordNavigator = recordNavigator,
                    infoNavigator = infoNavigator, context = this,
                )
            }
        }
    }
}