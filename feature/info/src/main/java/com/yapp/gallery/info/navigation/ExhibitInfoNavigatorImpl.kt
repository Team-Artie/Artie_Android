package com.yapp.gallery.info.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.yapp.gallery.navigation.info.ExhibitInfoNavigator
import javax.inject.Inject

class ExhibitInfoNavigatorImpl @Inject constructor(
) : ExhibitInfoNavigator {
    override fun provideGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController,
        navigateToCamera : (Long) -> Unit, navigateToGallery : (Long, Int) -> Unit
    ) {
        navGraphBuilder.infoGraph(
            navHostController = navController,
            navigateToCamera = navigateToCamera,
            navigateToGallery = navigateToGallery,
        )
    }
}