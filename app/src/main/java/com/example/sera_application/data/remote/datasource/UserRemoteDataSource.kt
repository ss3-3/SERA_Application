package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.dto.UserListResponse
import com.example.sera_application.domain.model.dto.UserProfileUpdateRequest

interface UserRemoteDataSource {
    suspend fun getUserById(userId: String): User?
    suspend fun updateUserProfile(userId: String, request: UserProfileUpdateRequest): User?
    suspend fun deleteUser(userId: String): Boolean
    suspend fun getAllUsers(page: Int, pageSize: Int): UserListResponse
    suspend fun approveOrganizer(userId: String): User?
    suspend fun suspendUser(userId: String, reason: String?): Boolean
    suspend fun reactivateUser(userId: String): Boolean
}