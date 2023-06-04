package com.yapp.gallery.info.navigation

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.yapp.gallery.info.ui.edit.ExhibitEditScreen
import com.yapp.gallery.info.ui.info.ExhibitInfoRoute
import org.json.JSONObject
import java.time.LocalDate

fun NavGraphBuilder.infoGraph(
    navHostController: NavHostController,
    navigateToGallery: (Long, Int) -> Unit,
    navigateToCamera: (Long) -> Unit,
) {
    navigation(startDestination = "info", route = "info_route") {
        composable("info?exhibitId={exhibitId},idToken={idToken}", arguments = listOf(
            navArgument("exhibitId") {
                type = NavType.LongType
            },
            navArgument("idToken"){
                type = NavType.StringType
            }
        )) {
            ExhibitInfoRoute(
                exhibitId = it.arguments?.getLong("exhibitId") ?: 0L,
                navigateToEdit = { payload -> navigateWithPayload(payload, navHostController) },
                navigateToGallery = { id, count ->
                    navigateToGallery(id, count)
                },
                navigateToCamera = {
                    navigateToCamera(it)
                },
//                    cameraLauncher.launch(
//                        cameraNavigator.navigate(context)
//                            .putExtra("postId", exhibitId)
//                    )
                navigateToWebPage = { navigateToWebPage(navHostController.context, it) },
                popBackStack = { navHostController.popBackStack() },
            )
        }
        composable(
            route = "edit?id={id},exhibitName={exhibitName},exhibitDate={exhibitDate},categoryId={categoryId},exhibitLink={exhibitLink}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
                navArgument("exhibitName") {
                    type = NavType.StringType
                },
                navArgument("exhibitDate") {
                    type = NavType.StringType
                },
                navArgument("categoryId") {
                    type = NavType.LongType
                }, navArgument("exhibitLink") {
                    type = NavType.StringType
                }
            )
        ) {
            ExhibitEditScreen(
                navigateToHome = {
                    navHostController.navigate("home")
//                    navigateToScreen(context, homeNavigator.navigate(context))
                },
                popBackStack = { navHostController.popBackStack() },
            )
        }
    }
}

//private fun popInfoScreen(navHostController: NavHostController){
//    if (navHostController.previousBackStackEntry != null) {
//        navHostController.popBackStack()
//    }
//    else{
//        navHostController.navigate("home")
//    }
//}

//private fun popBackStack(
//    context: Activity, navHostController: NavHostController
//){
//    if (navHostController.previousBackStackEntry != null) {
//        navHostController.popBackStack()
//    }
//    else{
//        context.finish()
//    }
//}

private fun navigateToWebPage(context: Context, it: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri()))
}

private fun navigateWithPayload(
    payload: String,
    navHostController: NavHostController
){
    val json = JSONObject(payload)

    val date = LocalDate.parse(json.getString("postDate"))

    val id = json.getLong("id")
    val exhibitName = json.getString("name")
    val categoryId = json.getLong("categoryId")
    var exhibitLink = json.getString("attachedLink")

    if (exhibitLink == "null") exhibitLink = ""

    val exhibitDate = "${date.year}/${date.monthValue}/${date.dayOfMonth}"

    navHostController.navigate("edit?" + "id=${id}," + "exhibitName=${exhibitName},"
            + "exhibitDate=${exhibitDate}," + "categoryId=${categoryId},"+"exhibitLink=${exhibitLink}")
}