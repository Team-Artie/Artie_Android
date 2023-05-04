package com.yapp.gallery.home.ui.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.home.navigation.HomeNavHost
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.gallery.navigation.record.RecordNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    @Inject lateinit var profileNavigator: ProfileNavigator
    @Inject lateinit var infoNavigator: ExhibitInfoNavigator
    @Inject lateinit var recordNavigator: RecordNavigator

    private lateinit var navController : NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            navController = rememberNavController()
            ArtieTheme {
                HomeNavHost(
                    navHostController = navController, profileNavigator = profileNavigator,
                    recordNavigator = recordNavigator, infoNavigator = infoNavigator,
                    context = this
                )
            }
        }
    }
}