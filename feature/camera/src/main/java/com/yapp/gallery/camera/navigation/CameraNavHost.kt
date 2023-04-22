package com.yapp.gallery.camera.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yapp.gallery.camera.model.ImageData
import com.yapp.gallery.camera.ui.camera.CameraRoute
import com.yapp.gallery.camera.ui.result.ResultRoute

@Composable
fun CameraNavHost(
    navController: NavHostController = rememberNavController(),
    context: Activity,
) {
    var localByteArray = remember<ByteArray?> { null }

    val startDestination = if (context.intent.hasExtra("gallery")) {
        CameraRoute.Result.name
    } else {
        CameraRoute.Camera.name
    }

    NavHost(navController = navController, startDestination = startDestination){
        composable(CameraRoute.Camera.name) {
            CameraRoute(
                navigateToResult = { byteArray ->
                    localByteArray = byteArray
                    navController.navigate(CameraRoute.Result.name) },
                popBackStack = { popBackStack(context, navController) }, context = context)
        }
        composable(CameraRoute.Result.name
        ) {
            ResultRoute(byteArray = localByteArray, context = context, popBackStack = { popBackStack(context, navController) })
        }
    }
}

private fun popBackStack(
    context: Activity, navHostController: NavHostController
) {
    if (navHostController.previousBackStackEntry != null) {
        navHostController.popBackStack()
    } else {
        context.finish()
    }
}

enum class CameraRoute {
    Camera, Result
}