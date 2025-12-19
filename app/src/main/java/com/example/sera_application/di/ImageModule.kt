package com.example.sera_application.di

import android.content.Context
import com.example.sera_application.data.local.image.LocalImageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideLocalImageManager(
        @ApplicationContext context: Context
    ): LocalImageManager {
        return LocalImageManager(context)
    }
}