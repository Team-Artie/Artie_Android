package com.yapp.gallery.home.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yapp.gallery.home.ui.home.HomeRoute
import com.yapp.gallery.home.ui.test.TestRoute
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.gallery.navigation.record.RecordNavigator

@Composable
fun HomeNavHost(
    navHostController: NavHostController,
    profileNavigator: ProfileNavigator,
    recordNavigator: RecordNavigator,
    infoNavigator: ExhibitInfoNavigator,
    context: Activity,
) {
    NavHost(navController = navHostController, startDestination = "home") {
        composable("home") {
            HomeRoute(
                navigateToRecord = {
                    navigateToScreen(
                        context,
                        recordNavigator.navigate(context)
                    )
                },
                navigateToProfile = {
                    navigateToScreen(
                        context,
                        profileNavigator.navigate(context)
                    )
                },
                navigateToInfo = { id, token ->
                    navigateToScreen(
                        context,
                        infoNavigator.navigateToInfo(context, id, token)
                    )
                },
                context = context,
                navigateToTest = {
                    navHostController.navigate("test?token=${it}")
                }
            )
        }
//        composable("record") {
//            ExhibitRecordRoute(
//                navigateToCamera = { postId ->
//                    navigateToScreen(
//                        context = context,
//                        intent = cameraNavigator.navigate(context)
//                            .putExtra("postId", postId)
//                    )
//                },
//                popBackStack = { popBackStack(context, navHostController) },
//                navigateToGallery = { postId ->
//                    navigateToScreen(
//                        context = context,
//                        intent = cameraNavigator.navigate(context).apply {
//                            putExtra("postId", postId)
//                            putExtra("gallery", true)
//                        }
//                    )
//                }
//            )
//        }
        composable("test?token={token}",
            arguments = listOf(
                navArgument("token"){
                    type = NavType.StringType
                }
            )
        ){
            val token = it.arguments?.getString("token")
            TestRoute(token = token)
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
