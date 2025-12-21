package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.NotificationEntity
import com.example.sera_application.domain.model.Notification
import com.example.sera_application.domain.model.enums.NotificationType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Implementation of NotificationMapper
 * Handles actual conversion logic between NotificationEntity and Notification
 *
 * Key conversions:
 * - String ↔ NotificationType enum
 * - Long timestamp ↔ LocalDateTime
 */
object NotificationMapperImpl {

    /**
     * Convert NotificationEntity (database) to Notification (domain model)
     *
     * Conversions:
     * - type: NotificationType enum → NotificationType enum
     * - createdAt: LocalDateTime → Long (timestamp)
     *
     * @param entity The notification entity from database
     * @return Notification domain model
     */
    fun toDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            userId = entity.userId,
            title = entity.title,
            message = entity.message,
            type = entity.type,
            relatedEventId = entity.relatedEventId,
            relatedReservationId = null, // NotificationEntity doesn't have this field
            isRead = entity.isRead,
            createdAt = entity.createdAt
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }

    /**
     * Convert Notification (domain model) to NotificationEntity (database)
     *
     * Conversions:
     * - type: NotificationType enum → NotificationType enum
     * - createdAt: Long (timestamp) → LocalDateTime
     *
     * @param domain The notification domain model
     * @return NotificationEntity for database storage
     */
    fun toEntity(domain: Notification): NotificationEntity {
        return NotificationEntity(
            id = domain.id,
            userId = domain.userId,
            title = domain.title,
            message = domain.message,
            type = domain.type,
            relatedEventId = domain.relatedEventId,
            relatedPaymentId = null, // Notification doesn't have this field
            isRead = domain.isRead,
            createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(domain.createdAt),
                ZoneId.systemDefault()
            )
        )
    }

    /**
     * Convert list of NotificationEntity to list of Notification
     *
     * Used when: Reading multiple notifications from database
     * Example: Getting all notifications for a user
     *
     * @param entities List of notification entities from database
     * @return List of notification domain models
     */
    fun toDomainList(entities: List<NotificationEntity>): List<Notification> {
        return entities.map { toDomain(it) }
    }

    /**
     * Convert list of Notification to list of NotificationEntity
     *
     * Used when: Batch saving notifications to database
     * Example: Saving multiple notifications at once
     *
     * @param domains List of notification domain models
     * @return List of notification entities for database storage
     */
    fun toEntityList(domains: List<Notification>): List<NotificationEntity> {
        return domains.map { toEntity(it) }
    }
}

