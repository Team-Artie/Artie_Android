package com.yapp.gallery.camera.ui.gallery

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.yapp.gallery.common.theme.color_background

@Composable
fun GalleryRoute(
    navigateToResult: (List<Uri>) -> Unit,
    popBackStack: () -> Unit,
    context: Activity
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)){
        if (it.isNotEmpty()){
            if (it.size > 5) {
                Toast.makeText(context, "사진은 5장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                navigateToResult(it.take(5))
            } else{
                navigateToResult(it)
            }
        } else {
            popBackStack()
        }
    }



    LaunchedEffect(Unit){
        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color_background))
}