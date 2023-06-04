package com.yapp.gallery.data.room.post

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yapp.gallery.data.room.post.TempPost
import com.yapp.gallery.data.room.post.TempPostDao

@Database(
    entities = [TempPost::class],
    version = 1
)
abstract class TempPostDatabase : RoomDatabase(){
    abstract fun tempPostDao(): TempPostDao
}