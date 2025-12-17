package com.example.sera_application.data.repository

import com.example.sera_application.data.remote.datasource.AuthRemoteDataSource
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Implementation of AuthRepository.
 * Coordinates authentication operations between remote datasource and domain layer.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val userId = authRemoteDataSource.login(email, password)
            val user = userRemoteDataSource.getUserProfile(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: String
    ): Result<User> {
        return try {
            val userId = authRemoteDataSource.register(email, password, fullName, role)
            val user = userRemoteDataSource.getUserProfile(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User profile not found after registration"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Boolean {
        return try {
            authRemoteDataSource.logout()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userId = authRemoteDataSource.getCurrentUserId()
            userId?.let { userRemoteDataSource.getUserProfile(it) }
        } catch (e: Exception) {
            null
        }
    }
}