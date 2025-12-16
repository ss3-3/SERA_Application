package com.example.sera_application.domain.usecase.auth

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user login
 * Handles authentication logic and returns the authenticated user
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validate input
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        // Call repository to perform login
        return authRepository.login(email, password)
    }
}