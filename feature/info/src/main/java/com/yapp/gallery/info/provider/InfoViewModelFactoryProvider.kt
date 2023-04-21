package com.yapp.gallery.info.provider

import com.yapp.gallery.info.ui.info.ExhibitInfoViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface InfoViewModelFactoryProvider {
    fun exhibitInfoViewModelFactory(): ExhibitInfoViewModel.InfoFactory
}