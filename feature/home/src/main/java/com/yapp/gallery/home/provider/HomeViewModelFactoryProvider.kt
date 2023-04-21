package com.yapp.gallery.home.provider

import com.yapp.gallery.home.ui.home.HomeViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface HomeViewModelFactoryProvider {
    fun homeViewModelFactory(): HomeViewModel.HomeFactory
}