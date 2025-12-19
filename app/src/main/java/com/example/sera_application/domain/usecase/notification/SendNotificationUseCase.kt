package com.example.sera_application.domain.usecase.notification

import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Use case for sending notifications to users
 * Handles notification delivery through FCM and local storage
 */
class SendNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        referenceId: String? = null
    ): Result<Unit> {
        // Validate input
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID cannot be empty"))
        }

        if (title.isBlank()) {
            return Result.failure(Exception("Notification title cannot be empty"))
        }

        if (message.isBlank()) {
            return Result.failure(Exception("Notification message cannot be empty"))
        }

        return try {
            notificationRepository.sendNotification(
                userId = userId,
                title = title,
                message = message,
                type = type,
                referenceId = referenceId
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}