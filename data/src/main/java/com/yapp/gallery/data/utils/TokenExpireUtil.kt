package com.yapp.gallery.data.utils


internal fun getTokenExpiredTime() : String {
    // 한시간 뒤 밀리 초
    return (System.currentTimeMillis() + 3500000).toString()
}

internal fun isTokenExpired(comparedTime: String) : Boolean{
    val currentTime = System.currentTimeMillis()
    val expiredTime = comparedTime.toLong()
    return currentTime > expiredTime
}