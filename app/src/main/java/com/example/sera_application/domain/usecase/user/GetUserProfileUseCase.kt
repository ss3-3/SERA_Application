package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): User? {
        if (userId.isBlank()) return null
        return userRepository.getUserById(userId)
    }
}