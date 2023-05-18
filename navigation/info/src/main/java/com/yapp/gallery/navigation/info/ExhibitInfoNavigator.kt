package com.yapp.gallery.navigation.info

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.yapp.gallery.core.Navigator

interface ExhibitInfoNavigator {
    fun provideGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController,
        navigateToCamera : (Long) -> Unit, navigateToGallery : (Long, Int) -> Unit)
}