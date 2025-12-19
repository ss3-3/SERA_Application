package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.repository.UserRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        return userRepository.deleteUser(userId)
    }
}
