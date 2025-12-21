package com.example.sera_application.domain.usecase.notification

import com.example.sera_application.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> {
        if (notificationId.isBlank()) {
            return Result.failure(Exception("Notification ID cannot be empty"))
        }
        return notificationRepository.markAsRead(notificationId)
    }
}

