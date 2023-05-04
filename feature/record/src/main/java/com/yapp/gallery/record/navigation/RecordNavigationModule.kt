package com.yapp.gallery.record.navigation

import com.yapp.gallery.navigation.record.RecordNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class RecordNavigationModule {
    @Binds
    abstract fun bindRecordNavigator(navigatorImpl: RecordNavigatorImpl) : RecordNavigator
}