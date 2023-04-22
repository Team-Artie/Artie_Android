package com.yapp.gallery.home.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yapp.gallery.common.provider.WebViewProvider
import com.yapp.gallery.home.ui.calendar.CalendarScreen
import com.yapp.gallery.home.ui.home.HomeRoute
import com.yapp.gallery.home.ui.record.ExhibitRecordRoute
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.navigation.camera.CameraNavigator

@Composable
fun HomeNavHost(
    navHostController: NavHostController,
    profileNavigator: ProfileNavigator,
    cameraNavigator: CameraNavigator,
    infoNavigator: ExhibitInfoNavigator,
    webViewProvider: WebViewProvider,
    context: Activity,
    navToImagePicker: (Long) -> Unit,
) {
    NavHost(navController = navHostController, startDestination = "home") {
        composable("home") {
            HomeRoute(
                navigateToRecord = { navHostController.navigate("record") },
                navigateToProfile = {
                    navigateToScreen(
                        context,
                        profileNavigator.navigate(context)
                    )
                },
                navigateToCalendar = { navHostController.navigate("calendar") },
                navigateToInfo = { id, token ->
                    navigateToScreen(
                        context,
                        infoNavigator.navigateToInfo(context, id, token)
                    )
                },
                webViewProvider = webViewProvider,
                context = context
            )
        }
        composable("record") {
            ExhibitRecordRoute(
                navigateToCamera = { postId ->
                    navigateToScreen(
                        context = context,
                        intent = cameraNavigator.navigate(context)
                            .putExtra("postId", postId)
                    )
                },
                popBackStack = { popBackStack(context, navHostController) },
                navigateToGallery = { postId -> navToImagePicker.invoke(postId) },
//                navigateToHome = {
//                    navHostController.navigate("home") {
//                        popUpTo(navHostController.graph.id) {
//                            inclusive = true
//                        }
//                    }
//                }
            )
        }
        composable("calendar") {
            CalendarScreen(
                popBackStack = { popBackStack(context, navHostController) },
                webViewProvider = webViewProvider
            )
        }
    }
}

private fun navigateToScreen(context: Context, intent: Intent) {
    context.startActivity(intent)
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
