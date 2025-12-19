package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.UserRole

data class User(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String? = null,
    val role: UserRole = UserRole.PARTICIPANT,
    val profileImagePath: String? = null,
    val accountStatus: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

