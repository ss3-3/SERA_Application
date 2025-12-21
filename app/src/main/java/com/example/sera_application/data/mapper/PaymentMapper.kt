package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.PaymentEntity
import com.example.sera_application.domain.model.Payment
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Interface for Payment mapping operations
 * Defines contract for converting between Entity (database) and Domain (business logic)
 */
interface PaymentMapper {

    /**
     * Convert PaymentEntity (database) to Payment (domain model)
     * @param entity The payment entity from database
     * @return Payment domain model
     */
    fun toDomain(entity: PaymentEntity): Payment

    /**
     * Convert Payment (domain model) to PaymentEntity (database)
     * @param domain The payment domain model
     * @return PaymentEntity for database storage
     */
    fun toEntity(domain: Payment): PaymentEntity

    /**
     * Convert list of PaymentEntity to list of Payment
     * @param entities List of payment entities from database
     * @return List of payment domain models
     */
    fun toDomainList(entities: List<PaymentEntity>): List<Payment>

    /**
     * Convert list of Payment to list of PaymentEntity
     * @param domains List of payment domain models
     * @return List of payment entities for database storage
     */
    fun toEntityList(domains: List<Payment>): List<PaymentEntity>
}

object PaymentFirestoreMapper {
    fun paymentToFirestoreMap(payment: Payment): Map<String, Any?> {
        return mapOf(
            "paymentId" to payment.paymentId,
            "userId" to payment.userId,
            "eventId" to payment.eventId,
            "reservationId" to payment.reservationId,
            "amount" to payment.amount,
            "status" to payment.status.name,
            "createdAt" to payment.createdAt
        )
    }

    fun DocumentSnapshot.toPayment(): Payment? {
        return try {
            val data = this.data ?: return null
            Payment(
                paymentId = this.id,
                userId = data["userId"]?.toString() ?: "",
                eventId = data["eventId"]?.toString() ?: "",
                reservationId = data["reservationId"]?.toString(),
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                status = try {
                    com.example.sera_application.domain.model.enums.PaymentStatus.valueOf(
                        data["status"]?.toString() ?: "PENDING"
                    )
                } catch (e: IllegalArgumentException) {
                    com.example.sera_application.domain.model.enums.PaymentStatus.PENDING
                },
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}