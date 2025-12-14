package com.example.sera_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey
    val reservationId: String,
    val eventId: String,
    val userId: String,
    val seats: Int,
    val status: String, // Store as String, convert to/from ReservationStatus enum
    val createdAt: Long
)