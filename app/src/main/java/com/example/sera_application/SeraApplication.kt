package com.example.sera_application

import android.app.Application
import android.util.Log
import com.example.sera_application.utils.FirebaseEventInitializer
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class SeraApplication : Application() {

    // Application scope for background tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Optional: Initialize predefined events in Firebase
        // Set to true to automatically create 4 predefined events on app startup
        // Set to false to disable automatic initialization
        val shouldInitializeEvents = false  // Change to true to enable

        if (shouldInitializeEvents) {
            applicationScope.launch {
                try {
                    FirebaseEventInitializer.initializePredefinedEvents(
                        FirebaseFirestore.getInstance()
                    )
                } catch (e: Exception) {
                    Log.e("SeraApplication", "Failed to initialize events: ${e.message}", e)
                }
            }
        }
    }
}