package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.Payment

interface PaymentRemoteDataSource {

    suspend fun savePayment(payment: Payment): String // Returns paymentId

    suspend fun getPaymentByReservation(reservationId: String): Payment?

    suspend fun getPaymentById(paymentId: String): Payment?

    suspend fun getPaymentsByUser(userId: String): List<Payment>

    suspend fun updatePaymentStatus(
        paymentId: String,
        status: String
    )
}
