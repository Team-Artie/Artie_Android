package com.yapp.gallery.core

import android.content.Context
import android.content.Intent
import android.net.Uri

interface Navigator {
    fun navigate(context: Context) : Intent
}
