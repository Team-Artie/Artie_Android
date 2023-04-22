package com.yapp.gallery.camera.provider

import com.yapp.gallery.camera.ui.result.ResultViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ResultViewModelFactoryProvider {
    fun resultViewModelFactory(): ResultViewModel.ResultFactory
}