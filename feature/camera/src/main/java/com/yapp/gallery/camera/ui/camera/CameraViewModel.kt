package com.yapp.gallery.camera.ui.camera

import androidx.camera.core.CameraSelector
import com.yapp.gallery.camera.ui.camera.CameraContract.CameraEvent
import com.yapp.gallery.camera.ui.camera.CameraContract.CameraReduce
import com.yapp.gallery.camera.ui.camera.CameraContract.CameraSideEffect
import com.yapp.gallery.camera.ui.camera.CameraContract.CameraState
import com.yapp.gallery.common.base.BaseStateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
) : BaseStateViewModel<CameraState, CameraEvent, CameraReduce, CameraSideEffect>(CameraState()) {

    override fun handleEvents(event: CameraEvent) {
        when(event){
            is CameraEvent.PermissionGranted -> {
                updateState(CameraReduce.SetPermission(true))
            }
            is CameraEvent.PermissionDenied -> {
                updateState(CameraReduce.SetDenied(true))
            }
            is CameraEvent.RequestPermission -> {
                sendSideEffect(CameraSideEffect.NavigateToAppSetting)
            }
            is CameraEvent.OnClickCapture -> {
                sendSideEffect(CameraSideEffect.ImageCapture)
            }
            is CameraEvent.OnClickRotate -> {
                updateState(CameraReduce.SetCameraLensFacing)
            }
        }
    }

//    private fun saveTempImage(byteArray: ByteArray){
//        saveTempImageUseCase(byteArray)
//            .onErrorComplete()
//            .onEach { sendSideEffect(CameraSideEffect.NavigateToResult) }
//            .launchIn(viewModelScope)
//    }

    override fun reduceState(state: CameraState, reduce: CameraReduce): CameraState {
        return when(reduce){
            is CameraReduce.SetPermission -> {
                state.copy(
                    permissionGranted = reduce.granted,
                    denied = if (reduce.granted) false else state.denied
                )
            }
            is CameraReduce.SetDenied -> {
                state.copy(denied = reduce.denied)
            }
            is CameraReduce.SetCameraLensFacing -> {
                state.copy(
                    lensFacing = if (state.lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
                        else CameraSelector.LENS_FACING_BACK)
            }
        }
    }
}
