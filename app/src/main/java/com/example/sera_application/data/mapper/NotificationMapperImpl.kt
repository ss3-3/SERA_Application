package com.example.sera_application.data.mapper
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import com.example.sera_application.data.local.entity.NotificationEntity
//import com.example.sera_application.domain.model.Notification
//import com.example.sera_application.domain.model.enums.NotificationType
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//import kotlin.collections.map
//
///**
// * Implementation of NotificationMapper
// * Handles actual conversion logic between NotificationEntity and Notification
// *
// * Key conversions:
// * - String ↔ NotificationType enum
// * - Long timestamp ↔ LocalDateTime
// */
//object NotificationMapperImpl : NotificationMapper {
//
//    /**
//     * Convert NotificationEntity (database) to Notification (domain model)
//     *
//     * Conversions:
//     * - type: String → NotificationType enum
//     * - createdAt: Long (timestamp) → LocalDateTime
//     *
//     * @param entity The notification entity from database
//     * @return Notification domain model
//     */
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun toDomain(entity: NotificationEntity): Notification {
//        return Notification(
//            id = entity.id,
//            userId = entity.userId,
//            title = entity.title,
//            message = entity.message,
//            type = try {
//                NotificationType.valueOf(entity.type)  // String → Enum
//            } catch (e: IllegalArgumentException) {
//                // If database has invalid type, fallback to SYSTEM
//                NotificationType.SYSTEM
//            },
//            relatedEventId = entity.relatedEventId,
//            relatedPaymentId = entity.relatedPaymentId,
//            relatedReservationId = entity.relatedReservationId,  // ← NEW: Added reservation
//            isRead = entity.isRead,
//            createdAt = LocalDateTime.ofInstant(
//                Instant.ofEpochMilli(entity.createdAt),  // Long → LocalDateTime
//                ZoneId.systemDefault()
//            )
//        )
//    }
//
//    /**
//     * Convert Notification (domain model) to NotificationEntity (database)
//     *
//     * Conversions:
//     * - type: NotificationType enum → String
//     * - createdAt: LocalDateTime → Long (timestamp in milliseconds)
//     *
//     * @param domain The notification domain model
//     * @return NotificationEntity for database storage
//     */
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun toEntity(domain: Notification): NotificationEntity {
//        return NotificationEntity(
//            id = domain.id,
//            userId = domain.userId,
//            title = domain.title,
//            message = domain.message,
//            type = domain.type.name,  // Enum → String (e.g., "EVENT_UPDATE")
//            relatedEventId = domain.relatedEventId,
//            relatedPaymentId = domain.relatedPaymentId,
//            relatedReservationId = domain.relatedReservationId,  // ← NEW: Added reservation
//            isRead = domain.isRead,
//            createdAt = domain.createdAt
//                .atZone(ZoneId.systemDefault())
//                .toInstant()
//                .toEpochMilli()  // LocalDateTime → Long timestamp
//        )
//    }
//
//    /**
//     * Convert list of NotificationEntity to list of Notification
//     *
//     * Used when: Reading multiple notifications from database
//     * Example: Getting all notifications for a user
//     *
//     * @param entities List of notification entities from database
//     * @return List of notification domain models
//     */
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun toDomainList(entities: List<NotificationEntity>): List<Notification> {
//        return entities.map { toDomain(it) }
//    }
//
//    /**
//     * Convert list of Notification to list of NotificationEntity
//     *
//     * Used when: Batch saving notifications to database
//     * Example: Saving multiple notifications at once
//     *
//     * @param domains List of notification domain models
//     * @return List of notification entities for database storage
//     */
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun toEntityList(domains: List<Notification>): List<NotificationEntity> {
//        return domains.map { toEntity(it) }
//    }
//}
//
