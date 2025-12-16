package com.example.sera_application.domain.usecase.auth

import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user logout
 * Handles user sign out and session cleanup
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return try {
            authRepository.logout()
        } catch (e: Exception) {
            false
        }
    }
}