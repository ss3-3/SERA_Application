package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val role: String, // Store as String, convert to/from UserRole enum
    val profileImagePath: String?,
    val accountStatus: String,
    val isApproved: Boolean = true,
    val emailVerified: Boolean = false,
    val approvalStatus: String? = null, // Store as String, convert to/from ApprovalStatus enum
    val approvedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)