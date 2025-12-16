package com.example.sera_application.domain.model.dto

/**
 * Request model for updating user profile information.
 * Only includes fields that can be updated by the user.
 */
data class UserProfileUpdateRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val profileImagePath: String? = null
)
