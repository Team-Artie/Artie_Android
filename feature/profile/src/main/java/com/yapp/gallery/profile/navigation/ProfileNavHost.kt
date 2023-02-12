package com.yapp.gallery.profile.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yapp.gallery.profile.screen.category.CategoryManageScreen
import com.yapp.gallery.profile.screen.legacy.LegacyScreen
import com.yapp.gallery.profile.screen.profile.ProfileScreen

@Composable
fun ProfileNavHost(
    logout : () -> Unit,
    withdrawal : () -> Unit,
    context: Activity
){
    val navHostController = rememberNavController()
    NavHost(navController = navHostController, startDestination = "profile"){
        composable("profile"){
            ProfileScreen(
                navigateToManage = { navHostController.navigate("manage") },
                navigateToLegacy = { navHostController.navigate("legacy") },
                logout = { logout() },
                withdrawal = { withdrawal() },
                popBackStack = { popBackStack(context, navHostController) },
            )
        }
        composable("manage"){
            CategoryManageScreen(popBackStack = { popBackStack(context, navHostController) })
        }
        composable("legacy"){
            LegacyScreen(
                popBackStack = { popBackStack(context, navHostController) },
                navigateToWebPage = { navigateToWebPage(context, it) }
            )
        }
    }
}

private fun popBackStack(
    context: Activity, navHostController: NavHostController
){
    Log.e("back", navHostController.backQueue.toString())
    if (navHostController.previousBackStackEntry != null) {
        navHostController.popBackStack()
    }
    else{
        context.finish()
    }
}

private fun navigateToWebPage(
    context: Activity, @StringRes linkRes: Int
){
    with(context){
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(linkRes))))
    }
}