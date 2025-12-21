package com.example.sera_application.domain.usecase.auth

import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for deleting user account.
 * Handles complete account deletion including Firebase Auth and Firestore data.
 */
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            authRepository.deleteAccount()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

