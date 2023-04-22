package com.yapp.gallery.camera.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yapp.gallery.camera.ui.camera.CameraRoute

@Composable
fun CameraNavHost(
    navController: NavHostController = rememberNavController(),
    context: Activity,
) {
    val startDestination = if (context.intent.hasExtra("gallery")) {
        CameraRoute.Saver.name
    } else {
        CameraRoute.Camera.name
    }

    NavHost(navController = navController, startDestination = startDestination){
        composable(CameraRoute.Camera.name) {
            CameraRoute(popBackStack = { context.finish() }, context = context)
        }
        composable(CameraRoute.Saver.name) {
        }
    }
}

enum class CameraRoute {
    Camera, Saver
}