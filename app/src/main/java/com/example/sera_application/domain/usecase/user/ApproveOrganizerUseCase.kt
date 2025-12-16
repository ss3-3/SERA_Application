package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.domain.exception.UserException
import javax.inject.Inject
//
//class ApproveOrganizerUseCase @Inject constructor(
//    private val userRepository: UserRepository
//) {
//    // requesterId is the ID of the admin performing the action
//    suspend operator fun invoke(requesterId: String, targetUserId: String): Result<User> {
//        if (requesterId.isBlank() || targetUserId.isBlank()) {
//             return Result.failure(IllegalArgumentException("User IDs cannot be blank"))
//        }
//
//        // Check if requester is admin
//        val requesterResult = userRepository.getUserById(requesterId)
//        val requester = requesterResult.getOrElse { return Result.failure(it) }
//
//        if (requester.role != UserRole.ADMIN) {
//             return Result.failure(UserException.UnauthorizedUserException(requesterId, "Approve Organizer"))
//        }
//
//        return userRepository.approveOrganizer(targetUserId)
//    }
//}