package com.example.sera_application.domain.model.dto

import com.example.sera_application.domain.model.enums.UserRole

/**
 * Response model for user profile data.
 * Used when returning user information to the client.
 */
data class UserProfileResponse(
    val userId: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: UserRole,
    val profileImagePath: String?,
    val accountStatus: String,
    val createdAt: Long,
    val updatedAt: Long
)
