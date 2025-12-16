package com.example.sera_application.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application module for dependency injection.
 * 
 * Note: Application Context is automatically provided by Hilt via @ApplicationContext.
 * Use @ApplicationContext Context in any @Provides or constructor that needs it.
 * 
 * No explicit providers needed here unless you need to provide
 * application-specific instances (e.g., custom coroutine dispatchers).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Application Context is automatically provided by Hilt
}