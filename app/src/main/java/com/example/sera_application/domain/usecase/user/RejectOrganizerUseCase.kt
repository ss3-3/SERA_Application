package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject

class RejectOrganizerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        if (userId.isBlank()) return false

        return try {
            userRepository.rejectOrganizer(userId)
        } catch (e: Exception) {
            false
        }
    }
}

