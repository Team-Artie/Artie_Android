package com.yapp.gallery.camera.ui.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.yapp.gallery.camera.widget.PermissionRequestDialog
import com.yapp.gallery.camera.widget.PermissionType
import com.yapp.gallery.common.theme.color_background
import com.yapp.gallery.common.util.onCheckPermissions

@Composable
fun GalleryRoute(
    popBackStack: () -> Unit,
    onLaunchImagePicker : () -> Unit,
    context: Activity
) {
    // 안드로이드 13 이전과 이후 분리
    val permission = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_EXTERNAL_STORAGE
    } else {
        Manifest.permission.READ_MEDIA_IMAGES
    }

    val permissionGranted = rememberSaveable { mutableStateOf(false) }
    val denied = rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ granted ->
        if (granted){
            permissionGranted.value = true
        } else{
            denied.value = true
        }
    }

    // 앱 세팅 런처
    val settingLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ){
            permissionGranted.value = true
            denied.value = false
        }
    }

    LaunchedEffect(Unit){
        context.onCheckPermissions(
            activity = context,
            permission = permission,
            onRequest = { permissionLauncher.launch(permission) },
            onGrant = { permissionLauncher.launch(permission) },
            onDeny = { permissionLauncher.launch(permission) }
        )
    }

    if (permissionGranted.value){
        LaunchedEffect(Unit){
            onLaunchImagePicker()
        }
    }

    if (denied.value){
        PermissionRequestDialog(
            type = PermissionType.GALLERY,
            onConfirm = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                settingLauncher.launch(intent)
            },
            onDismiss = popBackStack
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color_background))
}