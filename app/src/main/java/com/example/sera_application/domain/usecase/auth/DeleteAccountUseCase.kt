package com.example.sera_application.domain.usecase.auth

import android.util.Log
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.domain.usecase.event.CancelAllOrganizerEventsUseCase
import javax.inject.Inject

/**
 * Use case for deleting user account.
 * Handles complete account deletion including Firebase Auth and Firestore data.
 * If user is an organizer, cancels all their events before deletion.
 */
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val cancelAllOrganizerEventsUseCase: CancelAllOrganizerEventsUseCase
) {
    suspend operator fun invoke(): Result<Boolean> {
        return try {
            // Get current user to check if they are an organizer
            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.userId
            
            if (userId == null) {
                return Result.failure(Exception("No user logged in"))
            }
            
            // If user is an organizer, cancel all their events before deleting account
            if (currentUser.role == UserRole.ORGANIZER) {
                Log.d("DeleteAccountUseCase", "Deleting organizer account $userId, cancelling all events")
                val cancelSuccess = cancelAllOrganizerEventsUseCase(userId)
                if (!cancelSuccess) {
                    Log.w("DeleteAccountUseCase", "Failed to cancel some events for organizer: $userId, but continuing with account deletion")
                }
            }
            
            // Delete the account
            authRepository.deleteAccount()
        } catch (e: Exception) {
            Log.e("DeleteAccountUseCase", "Error deleting account: ${e.message}", e)
            Result.failure(e)
        }
    }
}

