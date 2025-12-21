package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.User

interface UserRemoteDataSource {
    
    suspend fun getUserProfile(userId: String): User?
    
    suspend fun updateUserProfile(user: User)
    
    suspend fun getAllUsers(): List<User>

    suspend fun getPendingOrganizers(): List<User>

    suspend fun updateApprovalStatus(userId: String, isApproved: Boolean)

    suspend fun deleteUser(userId: String)
}