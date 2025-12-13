package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.UserRole

data class User(
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

