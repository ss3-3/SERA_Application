package com.example.sera_application.domain.usecase.auth

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for getting the currently authenticated user
 * Returns the current user's information if logged in
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}