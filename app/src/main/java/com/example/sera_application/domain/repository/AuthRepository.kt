package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.User

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<User>

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: String
    ): Result<User>

    suspend fun logout(): Boolean

    suspend fun getCurrentUser(): User?
}