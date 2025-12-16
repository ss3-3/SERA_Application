package com.example.sera_application.data.remote.firebase

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.dto.UserListResponse
import com.example.sera_application.domain.model.dto.UserProfileUpdateRequest

interface FirebaseUserDataSource {
    suspend fun getUserDocument(userId: String): User?
    suspend fun updateUserDocument(userId: String, data: Map<String, Any>): User?
    suspend fun deleteUserDocument(userId: String)
    suspend fun getAllUserDocuments(limit: Int, offset: Int): UserListResponse
}