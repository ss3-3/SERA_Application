package com.example.sera_application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SeraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Optional: Initialize libraries like Firebase, etc.
    }
}