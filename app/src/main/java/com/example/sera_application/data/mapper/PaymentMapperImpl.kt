package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.PaymentEntity
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import javax.inject.Inject

/**
 * Implementation of PaymentMapper
 * Handles conversion between PaymentEntity and Payment
 *
 * Key conversions:
 * - String ↔ PaymentStatus enum
 */
class PaymentMapperImpl @Inject constructor() : PaymentMapper {

    override fun toDomain(entity: PaymentEntity): Payment {
        return Payment(
            paymentId = entity.paymentId,
            userId = entity.userId,
            eventId = entity.eventId,
            reservationId = entity.reservationId,
            amount = entity.amount,
            status = try {
                PaymentStatus.valueOf(entity.status)
            } catch (e: IllegalArgumentException) {
                PaymentStatus.PENDING
            },
            createdAt = entity.createdAt
        )
    }

    override fun toEntity(domain: Payment): PaymentEntity {
        return PaymentEntity(
            paymentId = domain.paymentId,
            userId = domain.userId,
            eventId = domain.eventId,
            reservationId = domain.reservationId,
            amount = domain.amount,
            status = domain.status.name,
            createdAt = domain.createdAt
        )
    }

    override fun toDomainList(entities: List<PaymentEntity>): List<Payment> {
        return entities.map { toDomain(it) }
    }

    override fun toEntityList(domains: List<Payment>): List<PaymentEntity> {
        return domains.map { toEntity(it) }
    }
}