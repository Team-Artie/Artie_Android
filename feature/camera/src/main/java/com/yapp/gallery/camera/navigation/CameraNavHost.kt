package com.yapp.gallery.camera.navigation

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yapp.gallery.camera.ui.camera.CameraRoute
import com.yapp.gallery.camera.ui.gallery.GalleryRoute
import com.yapp.gallery.camera.ui.result.ResultRoute

@Composable
fun CameraNavHost(
    navController: NavHostController = rememberNavController(),
    context: Activity,
) {
    var localByteArray = remember<ByteArray?> { null }
    val localUriList = remember { mutableListOf<Uri>()}

    val startDestination = if (context.intent.hasExtra("gallery")) {
        CameraRoute.Gallery.name
    } else {
        CameraRoute.Camera.name
    }

    NavHost(navController = navController, startDestination = startDestination){
        composable(CameraRoute.Camera.name) {
            CameraRoute(
                navigateToResult = { byteArray ->
                    localByteArray = byteArray
                    navController.navigate(CameraRoute.Result.name) },
                popBackStack = { popBackStack(context, navController) },
                context = context
            )
        }

        composable(CameraRoute.Gallery.name){
            GalleryRoute(
                navigateToResult = { list ->
                    localUriList.clear()
                    localUriList.addAll(list)
                    navController.navigate(CameraRoute.Result.name) },
                popBackStack = { popBackStack(context, navController) },
                context = context,
            )
        }

        composable(CameraRoute.Result.name) {
            ResultRoute(
                byteArray = localByteArray,
                context = context,
                uriList = localUriList,
                popBackStack = {
                    popBackStack(context, navController) }
            )
        }
    }
}

fun popBackStack(
    context: Activity, navHostController: NavHostController
) {

    if (navHostController.previousBackStackEntry != null) {
        navHostController.popBackStack()
    } else {
        context.finish()
    }
}

enum class CameraRoute {
    Camera, Gallery, Result
}