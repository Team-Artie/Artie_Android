package com.yapp.gallery.info.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import com.yapp.navigation.camera.CameraNavigator
import javax.inject.Inject

class ExhibitInfoNavigatorImpl @Inject constructor(
) : ExhibitInfoNavigator {
    override fun provideGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        val context = navController.context
        navGraphBuilder.infoGraph(
            navHostController = navController,
            navigateToCamera = {
                context.startActivity(
                    cameraNavigator.navigate(context)
                        .putExtra("postId", it)
                )
            },
            navigateToGallery = { id, count ->
                context.startActivity(
                    cameraNavigator.navigate(context)
                        .putExtra("postId", id)
                        .putExtra("count", count)
                        .putExtra("gallery", true)
                )
            },
        )
    }
}