package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject

class GetPendingOrganizersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<User> {
        return try {
            userRepository.getPendingOrganizers()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

