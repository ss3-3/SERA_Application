package com.example.sera_application.domain.usecase.auth

import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for updating user password.
 * Handles password update logic with validation.
 */
class UpdatePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String
    ): Boolean {
        // Validate input
        if (currentPassword.isBlank()) {
            return false
        }

        if (newPassword.isBlank()) {
            return false
        }

        // Use InputValidator for password validation
        val (isPasswordValid, _) = com.example.sera_application.utils.InputValidator.validatePassword(newPassword)
        if (!isPasswordValid) {
            return false
        }

        // Call repository to perform password update
        return authRepository.updatePassword(currentPassword, newPassword)
    }
}

