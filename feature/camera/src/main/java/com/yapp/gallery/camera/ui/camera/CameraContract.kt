package com.yapp.gallery.camera.ui.camera

import androidx.camera.core.CameraSelector
import com.yapp.gallery.common.base.ViewModelContract

class CameraContract {
    data class CameraState(
        val permissionGranted: Boolean = false,
        val denied: Boolean = false,
        val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    ) : ViewModelContract.State

    sealed class CameraEvent : ViewModelContract.Event {
        object PermissionGranted : CameraEvent()
        object PermissionDenied : CameraEvent()
        object RequestPermission : CameraEvent()
        object OnClickCapture : CameraEvent()
        object OnClickRotate : CameraEvent()
    }

    sealed class CameraReduce : ViewModelContract.Reduce {
        data class SetPermission(val granted: Boolean) : CameraReduce()
        data class SetDenied(val denied: Boolean) : CameraReduce()
        object SetCameraLensFacing : CameraReduce()
    }

    sealed class CameraSideEffect : ViewModelContract.SideEffect {
        object ImageCapture : CameraSideEffect()
        object NavigateToAppSetting : CameraSideEffect()
    }
}