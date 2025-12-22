package com.example.sera_application.domain.usecase.user

import android.util.Log
import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import javax.inject.Inject

class ApproveOrganizerUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        if (userId.isBlank()) return false

        return try {
            // Get user before approving to get email and name
            val user = userRepository.getUserById(userId)
            if (user == null) {
                Log.e("ApproveOrganizerUseCase", "User not found: $userId")
                return false
            }

            // Approve the organizer
            val success = userRepository.approveOrganizer(userId)
            if (!success) {
                Log.e("ApproveOrganizerUseCase", "Failed to approve organizer: $userId")
                return false
            }

            // Send notification to the organizer
            try {
                sendNotificationUseCase(
                    userId = userId,
                    title = "Organizer Account Approved",
                    message = "Your organizer account has been approved. You may now log in.",
                    type = NotificationType.ORGANIZER_APPROVAL
                ).onFailure { e ->
                    Log.e("ApproveOrganizerUseCase", "Failed to send notification: ${e.message}", e)
                    // Don't fail approval if notification fails
                }
            } catch (e: Exception) {
                Log.e("ApproveOrganizerUseCase", "Exception sending notification: ${e.message}", e)
                // Don't fail approval if notification fails
            }

            // Send email to the organizer
            try {
                val emailResult = authRepository.sendOrganizerApprovalEmail(user.email, user.fullName)
                if (emailResult.isSuccess) {
                    Log.d("ApproveOrganizerUseCase", "Approval email sent successfully to ${user.email}")
                } else {
                    Log.e("ApproveOrganizerUseCase", "Failed to send approval email to ${user.email}: ${emailResult.exceptionOrNull()?.message}")
                    // Don't fail approval if email fails, but log the error
                }
            } catch (e: Exception) {
                Log.e("ApproveOrganizerUseCase", "Exception sending approval email to ${user.email}: ${e.message}", e)
                // Don't fail approval if email fails
            }

            true
        } catch (e: Exception) {
            Log.e("ApproveOrganizerUseCase", "Error approving organizer: ${e.message}", e)
            false
        }
    }
}