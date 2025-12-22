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
            val data = this.data ?: run {
                android.util.Log.w("PaymentFirestoreMapper", "Document ${this.id} has no data")
                return null
            }

            // Try different field name variations for eventId
            val eventId = data["eventId"]?.toString()
                ?: data["event_id"]?.toString()
                ?: data["eventID"]?.toString()
                ?: ""

            if (eventId.isBlank()) {
                android.util.Log.w("PaymentFirestoreMapper", "Document ${this.id} has no eventId field")
            }

            val userId = data["userId"]?.toString()
                ?: data["user_id"]?.toString()
                ?: data["userID"]?.toString()
                ?: ""

            val reservationId = data["reservationId"]?.toString()
                ?: data["reservation_id"]?.toString()

            val amount = (data["amount"] as? Number)?.toDouble()
                ?: (data["amount"] as? String)?.toDoubleOrNull()
                ?: 0.0

            val statusStr = data["status"]?.toString() ?: "PENDING"
            android.util.Log.d("PaymentFirestoreMapper", "Document ${this.id} status from Firestore: $statusStr")

            val status = try {
                val parsedStatus = com.example.sera_application.domain.model.enums.PaymentStatus.valueOf(statusStr)

                // Log specifically for REFUND_PENDING
                if (parsedStatus == com.example.sera_application.domain.model.enums.PaymentStatus.REFUND_PENDING) {
                    android.util.Log.d("PaymentFirestoreMapper", "*** REFUND_PENDING status parsed successfully for document ${this.id}")
                }

                parsedStatus
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("PaymentFirestoreMapper", "Unknown payment status: $statusStr, defaulting to PENDING for document ${this.id}")
                com.example.sera_application.domain.model.enums.PaymentStatus.PENDING
            }

            val createdAt = (data["createdAt"] as? Long)
                ?: (data["created_at"] as? Long)
                ?: (data["createdAt"] as? Number)?.toLong()
                ?: System.currentTimeMillis()

            Payment(
                paymentId = this.id,
                userId = userId,
                eventId = eventId,
                reservationId = reservationId,
                amount = amount,
                status = status,
                createdAt = createdAt
            )
        } catch (e: Exception) {
            android.util.Log.e("PaymentFirestoreMapper", "Error converting document ${this.id} to Payment: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
}