package com.example.postsapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.postsapplication.data.local.dao.PostDao
import com.example.postsapplication.data.local.entity.PostEntity

@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PostsDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
