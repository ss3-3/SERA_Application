package com.example.sera_application.di

import com.example.sera_application.data.remote.paypal.repository.PayPalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PayPalModule {

    @Provides
    @Singleton
    fun providePayPalRepository(): PayPalRepository {
        // TODO: Move these to BuildConfig or secure storage
        val clientId = "Ae-f1v2M2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z" // Mock client ID
        val clientSecret = "Ea-1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0" // Mock client secret
        return PayPalRepository(clientId, clientSecret, isSandbox = true)
    }
}
