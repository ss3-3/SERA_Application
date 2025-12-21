package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.UserRole

/**
 * Domain model representing a User in the system.
 *
 * @property profileImagePath Local file path to the user's profile image.
 *   - Format: Absolute path string (e.g., "/data/user/0/com.example.app/files/images/user_123.jpg")
 *   - Can be null, empty, or placeholder string during Phase 1 (before LocalFileManager integration)
 *   - In Phase 2, will be populated by LocalFileManager when image is saved locally
 *   - UI should handle null/empty by showing default placeholder image
 */
data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String? = null,
    val role: UserRole = UserRole.PARTICIPANT,
    val profileImagePath: String? = null,
    val accountStatus: String = "ACTIVE",
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

