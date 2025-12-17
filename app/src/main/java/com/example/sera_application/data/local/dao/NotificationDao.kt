package com.example.sera_application.data.local.dao

import androidx.room.*
import com.example.sera_application.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Notification operations
 * Handles CRUD operations for notification history
 */
@Dao
interface NotificationDao {

    // ==========================================
    // INSERT OPERATIONS
    // ==========================================

    /**
     * Insert a new notification
     * @param notification The notification to insert
     * @return The row ID of the inserted notification
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    /**
     * Insert multiple notifications
     * @param notifications List of notifications to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    // ==========================================
    // QUERY OPERATIONS - BY USER
    // ==========================================

    /**
     * Get all notifications for a specific user
     * @param userId The user's ID
     * @return Flow of notification list (real-time updates)
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId ORDER BY created_at DESC")
    fun getNotificationsByUserId(userId: String): Flow<List<NotificationEntity>>

    /**
     * Get unread notifications for a specific user
     * @param userId The user's ID
     * @return Flow of unread notification list
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId AND is_read = 0 ORDER BY created_at DESC")
    fun getUnreadNotifications(userId: String): Flow<List<NotificationEntity>>

    /**
     * Get unread notification count for a specific user
     * @param userId The user's ID
     * @return Flow of unread count (for badge display)
     */
    @Query("SELECT COUNT(*) FROM notification WHERE user_id = :userId AND is_read = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    /**
     * Get a single notification by ID
     * @param notificationId The notification's ID
     * @return The notification entity or null
     */
    @Query("SELECT * FROM notification WHERE notification_id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    // ==========================================
    // QUERY OPERATIONS - BY TYPE & RELATIONS
    // ==========================================

    /**
     * Get notifications by type for a user
     * @param userId The user's ID
     * @param type The notification type (EVENT_UPDATE, PAYMENT, RESERVATION, SYSTEM)
     * @return Flow of filtered notification list
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId AND type = :type ORDER BY created_at DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<NotificationEntity>>

    /**
     * Get notifications related to a specific event
     * @param userId The user's ID
     * @param eventId The event's ID
     * @return Flow of event-related notifications
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId AND related_event_id = :eventId ORDER BY created_at DESC")
    fun getNotificationsByEventId(userId: String, eventId: String): Flow<List<NotificationEntity>>

    /**
     * Get notifications related to a specific reservation
     * @param userId The user's ID
     * @param reservationId The reservation's ID
     * @return Flow of reservation-related notifications
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId AND related_reservation_id = :reservationId ORDER BY created_at DESC")
    fun getNotificationsByReservationId(userId: String, reservationId: String): Flow<List<NotificationEntity>>

    /**
     * Get notifications related to a specific payment
     * @param userId The user's ID
     * @param paymentId The payment's ID
     * @return Flow of payment-related notifications
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId AND related_payment_id = :paymentId ORDER BY created_at DESC")
    fun getNotificationsByPaymentId(userId: String, paymentId: String): Flow<List<NotificationEntity>>

    // ==========================================
    // UPDATE OPERATIONS - MARK AS READ
    // ==========================================

    /**
     * Mark a notification as read
     * @param notificationId The notification's ID
     */
    @Query("UPDATE notification SET is_read = 1 WHERE notification_id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    /**
     * Mark all notifications as read for a user
     * @param userId The user's ID
     */
    @Query("UPDATE notification SET is_read = 1 WHERE user_id = :userId AND is_read = 0")
    suspend fun markAllAsRead(userId: String)

    /**
     * Mark multiple notifications as read
     * @param notificationIds List of notification IDs
     */
    @Query("UPDATE notification SET is_read = 1 WHERE notification_id IN (:notificationIds)")
    suspend fun markMultipleAsRead(notificationIds: List<String>)

    /**
     * Mark all notifications for a specific event as read
     * @param userId The user's ID
     * @param eventId The event's ID
     */
    @Query("UPDATE notification SET is_read = 1 WHERE user_id = :userId AND related_event_id = :eventId AND is_read = 0")
    suspend fun markEventNotificationsAsRead(userId: String, eventId: String)

    // ==========================================
    // DELETE OPERATIONS
    // ==========================================

    /**
     * Delete a notification
     * @param notification The notification to delete
     */
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    /**
     * Delete a notification by ID
     * @param notificationId The notification's ID
     */
    @Query("DELETE FROM notification WHERE notification_id = :notificationId")
    suspend fun deleteNotificationById(notificationId: String)

    /**
     * Delete all notifications for a user
     * @param userId The user's ID
     */
    @Query("DELETE FROM notification WHERE user_id = :userId")
    suspend fun deleteAllNotifications(userId: String)

    /**
     * Delete old read notifications (cleanup)
     * @param beforeTimestamp Delete notifications created before this timestamp
     */
    @Query("DELETE FROM notification WHERE is_read = 1 AND created_at < :beforeTimestamp")
    suspend fun deleteOldReadNotifications(beforeTimestamp: Long)

    /**
     * Delete all notifications related to a specific event
     * Useful when event is deleted
     * @param eventId The event's ID
     */
    @Query("DELETE FROM notification WHERE related_event_id = :eventId")
    suspend fun deleteNotificationsByEventId(eventId: String)

    /**
     * Delete all notifications related to a specific reservation
     * Useful when reservation is cancelled/deleted
     * @param reservationId The reservation's ID
     */
    @Query("DELETE FROM notification WHERE related_reservation_id = :reservationId")
    suspend fun deleteNotificationsByReservationId(reservationId: String)

    // ==========================================
    // UTILITY OPERATIONS
    // ==========================================

    /**
     * Get notification count for a user (total)
     * @param userId The user's ID
     * @return Total notification count
     */
    @Query("SELECT COUNT(*) FROM notification WHERE user_id = :userId")
    suspend fun getNotificationCount(userId: String): Int

    /**
     * Check if a notification exists
     * @param notificationId The notification's ID
     * @return True if exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM notification WHERE notification_id = :notificationId)")
    suspend fun notificationExists(notificationId: String): Boolean

    /**
     * Get latest notification for a user
     * @param userId The user's ID
     * @return The most recent notification or null
     */
    @Query("SELECT * FROM notification WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestNotification(userId: String): NotificationEntity?
}

