package com.yapp.gallery.record.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.navigation.camera.CameraNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExhibitRecordActivity : ComponentActivity() {
    @Inject lateinit var cameraNavigator : CameraNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArtieTheme {
                ExhibitRecordRoute(
                    navigateToCamera = { postId ->
                        startActivity(
                            cameraNavigator.navigate(this)
                                .putExtra("postId", postId)
                        )
                    },
                    popBackStack = { finish() },
                    navigateToGallery = { postId ->
                        startActivity(
                            cameraNavigator.navigate(this)
                                .putExtra("postId", postId)
                                .putExtra("gallery", true)
                        )
                    }
                )
            }
        }
    }
}