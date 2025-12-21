package com.example.sera_application.domain.usecase.notification

import com.example.sera_application.domain.model.Notification
import com.example.sera_application.domain.repository.NotificationRepository
import javax.inject.Inject

class GetUserNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Notification>> {
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID cannot be empty"))
        }
        return notificationRepository.getUserNotifications(userId)
    }
}

