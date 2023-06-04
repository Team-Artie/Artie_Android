package com.yapp.gallery.record.navigation

import android.content.Context
import android.content.Intent
import com.yapp.gallery.navigation.record.RecordNavigator
import com.yapp.gallery.record.ui.ExhibitRecordActivity
import javax.inject.Inject

class RecordNavigatorImpl @Inject constructor(
): RecordNavigator {
    override fun navigate(context: Context): Intent {
        return Intent(context, ExhibitRecordActivity::class.java)
    }
}