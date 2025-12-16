package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Boolean {
        return try {
            userRepository.updateUser(user)
        } catch (e: Exception) {
            false
        }
    }
}