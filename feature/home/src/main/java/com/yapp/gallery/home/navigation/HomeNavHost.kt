package com.yapp.gallery.home.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yapp.gallery.home.screen.calendar.CalendarScreen
import com.yapp.gallery.home.screen.edit.ExhibitEditScreen
import com.yapp.gallery.home.screen.home.HomeScreen
import com.yapp.gallery.home.screen.record.ExhibitRecordScreen
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.gallery.navigation.profile.ProfileNavigator
import com.yapp.navigation.camera.CameraNavigator

@Composable
fun HomeNavHost(
    navHostController: NavHostController,
    profileNavigator: ProfileNavigator,
    cameraNavigator: CameraNavigator,
    infoNavigator: ExhibitInfoNavigator,
    context: Activity,
    navToImagePicker: () -> Unit,
){
    NavHost(navController = navHostController, startDestination = "home"){
        composable("home"){ HomeScreen(
            navigateToRecord = { navHostController.navigate("record")},
            navigateToProfile = { navigateToScreen(context, profileNavigator.navigate(context))},
            navigateToCalendar = { navHostController.navigate("calendar")},
            navigateToInfo = { navigateToScreen(context, infoNavigator.navigateToInfo(context, it))}
        ) }
        composable("record"){ ExhibitRecordScreen(
            navigateToCamera = { navigateToScreen(context, cameraNavigator.navigate(context)) },
            // Todo : 임시로 갤러리 대신 프로필로 가게 함
            navigateToGallery = { navigateToScreen(context, profileNavigator.navigate(context)) },
            popBackStack = { popBackStack(context, navHostController)}
            navigateToGallery = { navToImagePicker.invoke() }
            navigateToEdit = { navHostController.navigate("edit?id=19")}
        ) }
        composable("record"){ ExhibitRecordScreen(
            navigateToCamera = { navigateToScreen(context, cameraNavigator.navigate(context)) },
            navigateToGallery = { navToImagePicker.invoke() },
            popBackStack = { popBackStack(context, navHostController)}
        ) }
        composable("calendar") { CalendarScreen(

        ) }
//        // Todo : 나중에 따로 빼야함
//        composable(
//            route = "edit?id={id}",
//            arguments = listOf(
//                navArgument("id"){
//                    type = NavType.LongType
//                }
//            )
//        ) {
//            com.yapp.gallery.info.screen.edit.ExhibitEditScreen(popBackStack = {
//                popBackStack(
//                    context,
//                    navHostController
//                )
//            })
//        }
    }
}

private fun navigateToScreen(context: Context, intent: Intent){
    context.startActivity(intent)
}

private fun popBackStack(
    context: Activity, navHostController: NavHostController
){

    if (navHostController.previousBackStackEntry != null) {
        navHostController.popBackStack()
    }
    else{
        context.finish()
    }
}