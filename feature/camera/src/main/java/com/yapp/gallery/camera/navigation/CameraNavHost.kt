package com.yapp.gallery.camera.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yapp.gallery.camera.ui.camera.CameraRoute
import com.yapp.gallery.camera.ui.gallery.GalleryRoute
import com.yapp.gallery.camera.ui.result.ResultRoute
import com.yapp.gallery.camera.util.uriToByteArray
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraNavHost(
    navController: NavHostController = rememberNavController(),
    onLaunchImagePicker : () -> Unit,
    resultFlow: MutableSharedFlow<List<String>>,
    context: Activity,
) {
    val localImageList = rememberSaveable { mutableListOf<String>()}

    val startDestination = if (context.intent.hasExtra("gallery")) {
        CameraRoute.Gallery.name
    } else {
        CameraRoute.Camera.name
    }

    LaunchedEffect(Unit){
        resultFlow.collectLatest {
            if (it.isNotEmpty()){
                localImageList.clear()
                localImageList.addAll(it)
                navController.navigate(CameraRoute.Result.name)
            } else {
                popBackStack(context, navController)
            }
        }
    }


    NavHost(navController = navController, startDestination = startDestination){
        composable(CameraRoute.Camera.name) {
            CameraRoute(
                navigateToResult = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("captureUri", it)
                    navController.navigate(CameraRoute.Result.name) },
                popBackStack = { popBackStack(context, navController) },
                context = context
            )
        }

        composable(CameraRoute.Gallery.name){
            GalleryRoute(
                popBackStack = { popBackStack(context, navController) },
                context = context,
                onLaunchImagePicker = onLaunchImagePicker
            )
        }

        composable(CameraRoute.Result.name) {
            val byteArrayList = localImageList.map { uriToByteArray(context, it.toUri()) }
            val captureUri = navController.previousBackStackEntry?.savedStateHandle?.get<Uri>("captureUri")
            ResultRoute(
                byteArray = if (captureUri != null) uriToByteArray(context, captureUri) else null,
                context = context,
                imageList = byteArrayList,
                popBackStack = {
                    popBackStack(context, navController)
                },
                navigateToInfo = {
                    with(context){
                        val intent = Intent()
                        val extras = Bundle()
                        extras.putLong("exhibitId", it)
                        intent.putExtras(extras)

                        setResult(Activity.RESULT_OK, intent)
                        finish()
//                        startActivity(homeNavigator.navigate(context)
//                            .putExtra("exhibitId", it)
//                            .putExtra("fromResult", true)
//                        )
                    }
                }
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