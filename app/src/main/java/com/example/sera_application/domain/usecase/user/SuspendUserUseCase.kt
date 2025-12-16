package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.domain.exception.UserException
import javax.inject.Inject

class SuspendUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // requesterId is the ID of the admin performing the action
    suspend operator fun invoke(requesterId: String, targetUserId: String, reason: String): Result<Unit> {
        if (requesterId.isBlank() || targetUserId.isBlank()) {
            return Result.failure(IllegalArgumentException("User IDs cannot be blank"))
        }

        if (reason.isBlank()) {
             return Result.failure(IllegalArgumentException("Suspension reason is required"))
        }

        // Check if requester is admin
        val requesterResult = userRepository.getUserById(requesterId)
        val requester = requesterResult.getOrElse { return Result.failure(it) }

        if (requester.role != UserRole.ADMIN) {
             return Result.failure(UserException.UnauthorizedUserException(requesterId, "Suspend User"))
        }

        return userRepository.suspendUser(targetUserId, reason)
    }
}