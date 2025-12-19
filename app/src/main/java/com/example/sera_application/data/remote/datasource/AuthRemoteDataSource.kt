package com.example.sera_application.data.remote.datasource

interface AuthRemoteDataSource {
    
    suspend fun login(email: String, password: String): String // Returns userId
    
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): String // Returns userId
    
    suspend fun logout()
    
    suspend fun getCurrentUserId(): String?
    
    suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean
}