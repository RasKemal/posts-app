package com.example.postsapplication.di

import android.content.Context
import androidx.room.Room
import com.example.postsapplication.data.local.PostsDatabase
import com.example.postsapplication.data.local.dao.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PostsDatabase =
        Room.databaseBuilder(context, PostsDatabase::class.java, "posts.db").build()

    @Provides
    fun providePostDao(database: PostsDatabase): PostDao = database.postDao()
}
