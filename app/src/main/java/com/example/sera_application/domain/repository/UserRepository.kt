package com.example.sera_application.domain.repository

import com.example.sera_application.domain.model.User

interface UserRepository {

    suspend fun getUserById(userId: String): User?

    suspend fun getAllUsers(): List<User>

    suspend fun updateUser(user: User): Boolean

    suspend fun deleteUser(userId: String): Boolean

    suspend fun approveOrganizer(userId: String): Boolean

    suspend fun suspendUser(userId: String): Boolean
}