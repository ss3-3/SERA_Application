package com.example.sera_application.domain.usecase.auth

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user registration
 * Handles account creation with validation
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        role: String = "PARTICIPANT"
    ): Result<User> {
        // Validate input
        if (fullName.isBlank()) {
            return Result.failure(Exception("Full name cannot be empty"))
        }

        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(Exception("Invalid email format"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        // Call repository to perform registration
        return authRepository.register(fullName, email, password, role)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}