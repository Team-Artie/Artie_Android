package com.yapp.gallery.record.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.navigation.camera.CameraNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExhibitRecordActivity : ComponentActivity() {
    @Inject lateinit var cameraNavigator : CameraNavigator

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                val resultIntent = Intent()
                val extras = Bundle()
                extras.putLong("exhibitId", it.data?.extras?.getLong("exhibitId")?: 0L)
                resultIntent.putExtras(extras)

                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        setContent {
            ArtieTheme {
                ExhibitRecordRoute(
                    navigateToCamera = { postId ->
                        resultLauncher.launch(
                            cameraNavigator.navigate(this)
                                .putExtra("postId", postId)
                        )
//                        startActivity(
//                            cameraNavigator.navigate(this)
//                                .putExtra("postId", postId)
//                        )
                    },
                    popBackStack = { finish() },
                    navigateToGallery = { postId ->
                        resultLauncher.launch(
                            cameraNavigator.navigate(this)
                                .putExtra("postId", postId)
                                .putExtra("gallery", true)
                        )
//                        startActivity(
//                            cameraNavigator.navigate(this)
//                                .putExtra("postId", postId)
//                                .putExtra("gallery", true)
//                        )
                    }
                )
            }
        }
    }
}