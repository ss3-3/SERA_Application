package com.example.sera_application.domain.model.dto

/**
 * Response model for paginated user lists.
 * Used by admin/organizer to view multiple users.
 */
data class UserListResponse(
    val users: List<UserSummary>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int
)

/**
 * Simplified user information for list views.
 */
data class UserSummary(
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val accountStatus: String,
    val createdAt: Long
)
