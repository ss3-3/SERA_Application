package com.example.sera_application.domain.usecase.user

import com.example.sera_application.domain.exception.UserException
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.ValidationResult
import com.example.sera_application.domain.model.dto.UserProfileUpdateRequest
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.domain.util.UserValidator
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, request: UserProfileUpdateRequest): Result<User> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }

        // Validate request
        val validation = UserValidator.validateProfileUpdate(
            fullName = request.fullName,
            phone = request.phone,
            profileImagePath = request.profileImagePath
        )

        return when (validation) {
            is ValidationResult.Success -> {
                userRepository.updateUserProfile(userId, request)
            }
            is ValidationResult.Error -> {
                Result.failure(UserException.InvalidUserDataException(validation.field.orEmpty(), validation.message))
            }
            is ValidationResult.MultipleErrors -> {
                // Combine errors for simple result, or meaningful first error
                val firstError = validation.errors.first()
                Result.failure(UserException.InvalidUserDataException(firstError.field, firstError.message))
            }
        }
    }
}