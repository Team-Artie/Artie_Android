package com.yapp.gallery.camera.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.gallery.camera.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.yapp.gallery.camera.ui.camera.CameraContract.*
import com.yapp.gallery.camera.widget.PermissionRequestDialog
import com.yapp.gallery.camera.widget.PermissionType
import com.yapp.gallery.common.theme.GalleryTheme
import com.yapp.gallery.common.theme.color_popUpBottom
import com.yapp.gallery.common.util.onCheckPermissions
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraRoute(
    popBackStack: () -> Unit,
    context: Activity,
    viewModel: CameraViewModel = hiltViewModel(),
){
    val cameraState : CameraState by viewModel.viewState.collectAsStateWithLifecycle()

    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }

    // 권한 체크 런처
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted ->
        if (granted){
            viewModel.sendEvent(CameraEvent.PermissionGranted)
        } else{
            viewModel.sendEvent(CameraEvent.PermissionDenied)
        }
    }

    // 앱 세팅 런처
    val settingLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ){
            viewModel.sendEvent(CameraEvent.PermissionGranted)
        }
    }

    // 권한 체크
    LaunchedEffect(Unit){
        context.onCheckPermissions(
            activity = context,
            permission = Manifest.permission.CAMERA,
            onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onDeny = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    }

    LaunchedEffect(viewModel.sideEffect){
        // 앱 설정화면으로 이동
        viewModel.sideEffect.collectLatest {
            when(it){
                is CameraSideEffect.ImageCapture -> {
                    moveToResultScreen(
                        fileName = "yyyy-MM-dd-HH-mm-ss-SSS",
                        executor = ContextCompat.getMainExecutor(context),
                        outputDirectory = getFileOutput(context),
                        onImageCapture = { uri -> viewModel.sendEvent(CameraEvent.OnImageCapture(uri))},
                        imageCapture = imageCapture
                    )
                }
                is CameraSideEffect.NavigateToAppSetting -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    settingLauncher.launch(intent)
                }
            }
        }
    }

    // 권한 거부되었을 때
    if (cameraState.denied){
        PermissionRequestDialog(
            type = PermissionType.CAMERA,
            onConfirm = { viewModel.sendEvent(CameraEvent.RequestPermission) },
            onDismiss = popBackStack
        )
    }

    if (cameraState.permissionGranted){
        CameraScreen(
            cameraState = cameraState,
            imageCapture = imageCapture,
            onClickCapture = { viewModel.sendEvent(CameraEvent.OnClickCapture) },
            onClickRotate = { viewModel.sendEvent(CameraEvent.OnClickRotate) },
            popBackStack = popBackStack,
            context = context
        )
    }
}

@Composable
private fun CameraScreen(
    cameraState: CameraState,
    imageCapture: ImageCapture,
    onClickCapture: () -> Unit,
    onClickRotate: () -> Unit,
    popBackStack: () -> Unit,
    context: Activity
){
    val lifecycle = LocalLifecycleOwner.current
    val preview = CameraPreview.Builder().build()
    val previewView: PreviewView = remember { PreviewView(context) }

    var cameraSelector: CameraSelector

    LaunchedEffect(key1 = cameraState.lensFacing) {
        val cameraProvider = context.getCameraProvider()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraState.lensFacing)
            .build()

        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(
            lifecycle,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    if (cameraState.permissionGranted){
        CameraContent(
            previewView = previewView,
            onClickCapture = onClickCapture,
            onClickRotate = onClickRotate,
            popBackStack = popBackStack
        )
    }
}

@Composable
private fun CameraContent(
    previewView: PreviewView,
    onClickCapture: () -> Unit,
    onClickRotate: () -> Unit,
    popBackStack: () -> Unit
){
    Box(modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()) {
        // 카메라 프리뷰
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .alpha(0.7f)
                .background(Color.Black)
                .fillMaxWidth()
        ) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 27.dp),
                onClick = popBackStack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }


        ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 28.dp)
        ) {
            val (captureBtn, faceTurnBtn) = createRefs()

            IconButton(
                onClick = onClickCapture,
                modifier = Modifier.constrainAs(captureBtn) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera_capture),
                    contentDescription = "capture",
                    tint = Color.White
                )
            }

            IconButton(
                modifier = Modifier
                    .constrainAs(faceTurnBtn) {
                        start.linkTo(captureBtn.end)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                onClick = onClickRotate,
            ) {
                Surface(modifier = Modifier.size(53.dp), shape = CircleShape, color = color_popUpBottom) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_turn),
                        contentDescription = "turn",
                        modifier = Modifier.padding(11.dp)
                    )
                }
            }


        }

    }
}

@Preview(showBackground = true)
@Composable
fun CameraContentPreview(){
    GalleryTheme {
        CameraContent(
            previewView = PreviewView(LocalContext.current),
            onClickCapture = { /*TODO*/ },
            onClickRotate = { /*TODO*/ },
            popBackStack = { /*TODO*/ }
        )
    }
}




//@Composable
//fun CameraView(
//    outputDirectory: File,
//    onImageCapture: (Uri) -> Unit,
//    executor: Executor,
//    onDismiss: () -> Unit
//) {
//    val lensFacing = CameraSelector.LENS_FACING_BACK
//    val context = LocalContext.current
//    val lifecycle = LocalLifecycleOwner.current
//    val preview = CameraPreview.Builder().build()
//    val previewView: PreviewView = remember { PreviewView(context) }
//    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
//    val cameraSelector = CameraSelector.Builder()
//        .requireLensFacing(lensFacing)
//        .build()
//
//    LaunchedEffect(key1 = cameraSelector) {
//        val cameraProvider = context.getCameraProvider()
//
//        cameraProvider.unbindAll()
////        cameraProvider.bindToLifecycle(
////            lifecycle,
////            cameraSelector,
////            preview,
////            imageCapture
////        )
//        cameraProvider.bindToLifecycle(
//            lifecycle,
//            cameraSelector,
//            preview,
//            imageCapture
//        )
//
//        preview.setSurfaceProvider(previewView.surfaceProvider)
//    }
//
//    Box(contentAlignment = Alignment.BottomCenter) {
//
//        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
//
//        Box(
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .alpha(0.7f)
//                .background(Color.Black)
//                .fillMaxWidth()
//        ) {
//            IconButton(
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(top = 27.dp),
//                onClick = onDismiss
//            ) {
//                Icon(
//                    imageVector = Icons.Default.ArrowBack,
//                    contentDescription = null,
//                    tint = Color.White
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Start,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Spacer(modifier = Modifier.weight(1f))
//            IconButton(
//                modifier = Modifier
//                    .size(74.dp)
//                    .weight(1f),
//                onClick = {
//                    moveToResultScreen(
//                        fileName = "yyyy-MM-dd-HH-mm-ss-SSS",
//                        outputDirectory,
//                        imageCapture,
//                        executor,
//                        onImageCapture
//                    )
//                }
//            ) {
//                Icon(
//                    imageVector = Icons.Sharp.Lens,
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxSize(),
//                    tint = Color.White
//                )
//            }
//
//            IconButton(
//                modifier = Modifier
//                    .size(53.dp)
//                    .weight(1f),
//                onClick = { /*TODO*/ },
//            ) {
//                Icon(
//                    modifier = Modifier.size(50.dp),
//                    imageVector = Icons.TwoTone.ChangeCircle,
//                    contentDescription = null,
//                    tint = Color.White
//                )
//            }
//        }
//    }
//
//}

private fun moveToResultScreen(
    fileName: String,
    outputDirectory: File,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCapture: (Uri) -> Unit
) {
    val file = File(
        outputDirectory,
        SimpleDateFormat(fileName, Locale.KOREAN).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {}

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = Uri.fromFile(file)
            onImageCapture(savedUri)
        }
    })
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun getFileOutput(context: Activity): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}
