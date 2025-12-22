package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.NotificationEntity
import com.example.sera_application.domain.model.Notification
import com.example.sera_application.domain.model.enums.NotificationType

/**
 * Implementation of NotificationMapper
 * Handles actual conversion logic between NotificationEntity and Notification
 *
 * Key conversions:
 * - NotificationType enum (stored via type converter)
 * - Long timestamp (stored directly)
 */
object NotificationMapperImpl {

    /**
     * Convert NotificationEntity (database) to Notification (domain model)
     *
     * Conversions:
     * - type: NotificationType enum → NotificationType enum
     * - createdAt: Long → Long (timestamp, no conversion needed)
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
            createdAt = entity.createdAt // Already a Long timestamp
        )
    }

    /**
     * Convert Notification (domain model) to NotificationEntity (database)
     *
     * Conversions:
     * - type: NotificationType enum → NotificationType enum
     * - createdAt: Long → Long (timestamp, no conversion needed)
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
            createdAt = domain.createdAt // Already a Long timestamp
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

