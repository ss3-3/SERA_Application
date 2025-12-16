package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.dto.UserListResponse
import com.example.sera_application.domain.model.dto.UserProfileUpdateRequest

interface UserRepository {

    suspend fun getUserById(userId: String): Result<User>

    suspend fun getAllUsers(page: Int, pageSize: Int): Result<UserListResponse>

    suspend fun updateUserProfile(userId: String, request: UserProfileUpdateRequest): Result<User>

    suspend fun deleteUser(userId: String): Result<Unit>

    suspend fun approveOrganizer(userId: String): Result<User>

    suspend fun suspendUser(userId: String, reason: String?): Result<Unit>
    
    suspend fun reactivateUser(userId: String): Result<Unit>
}