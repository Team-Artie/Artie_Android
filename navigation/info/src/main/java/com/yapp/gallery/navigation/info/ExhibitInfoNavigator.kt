package com.yapp.gallery.navigation.info

import android.content.Context
import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.yapp.gallery.core.Navigator

interface ExhibitInfoNavigator : Navigator {
    fun navigateToInfo(context: Context, exhibitId: Long, idToken: String?) : Intent

}