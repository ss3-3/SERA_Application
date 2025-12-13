package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey
    val paymentId: String,
    val userId: String,
    val eventId: String,
    val reservationId: String?,
    val amount: Double,
    val status: String, // Store as String, convert to/from PaymentStatus enum
    val createdAt: Long
)