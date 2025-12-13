package com.example.sera_application.domain.model

import com.example.sera_application.domain.model.enums.PaymentStatus

data class Payment(
    val paymentId: String,
    val userId: String,
    val eventId: String,
    val reservationId: String?,
    val amount: Double,
    val status: PaymentStatus,
    val createdAt: Long
)
