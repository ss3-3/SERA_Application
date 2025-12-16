package com.example.sera_application.domain.model.dto

import com.example.sera_application.domain.model.enums.UserRole

/**
 * Request model for user registration.
 * Contains all required information for creating a new user account.
 */
data class UserRegistrationRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: UserRole = UserRole.PARTICIPANT
)
