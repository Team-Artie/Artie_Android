package com.yapp.gallery.home.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yapp.gallery.home.ui.home.HomeRoute
import com.yapp.gallery.home.ui.test.TestRoute
import com.yapp.gallery.info.navigation.infoGraph
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.login.LoginNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.gallery.navigation.record.RecordNavigator
import com.yapp.navigation.camera.CameraNavigator

@Composable
fun HomeNavHost(
    navHostController: NavHostController,
    loginNavigator: LoginNavigator,
    profileNavigator: ProfileNavigator,
    recordNavigator: RecordNavigator,
    cameraNavigator : CameraNavigator,
    context: Activity,
) {
    val recordLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            it.data?.extras?.getLong("exhibitId")?.let { id ->
                navHostController.navigate("info?exhibitId=${id},idToken=&${null}")
            }
        }
    }

    NavHost(navController = navHostController, startDestination = "home") {
        composable("home") {
            HomeRoute(
                navigateToRecord = {
                    recordLauncher.launch(recordNavigator.navigate(context))
                },
                navigateToProfile = {
                    navigateToScreen(
                        context,
                        profileNavigator.navigate(context)
                    )
                },
                navigateToInfo = { id, idToken ->
                    navHostController.navigate("info?exhibitId=${id},idToken=${idToken}")
                },
                context = context,
                navigateToTest = {
                    navHostController.navigate("test?token=${it}")
                },
                navigateToLogin = {
                    with(context){
                        finishAfterTransition()
                        startActivity(loginNavigator.navigate(this))
                    }
                }
            )
        }


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

        infoGraph(
            navHostController = navHostController,
            context = context,
            navigateToCamera = {
                navigateToScreen(context, cameraNavigator.navigate(context)
                    .putExtra("postId", it))
            },
            navigateToGallery = {
                navigateToScreen(context, cameraNavigator.navigate(context)
                    .putExtra("postId", it)
                    .putExtra("gallery", true))
            },
        )
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
