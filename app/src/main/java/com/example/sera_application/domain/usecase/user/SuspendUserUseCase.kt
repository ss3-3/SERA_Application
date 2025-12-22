package com.example.sera_application.domain.usecase.user

import android.util.Log
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.UserRepository
import com.example.sera_application.domain.usecase.event.CancelAllOrganizerEventsUseCase
import javax.inject.Inject

class SuspendUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val cancelAllOrganizerEventsUseCase: CancelAllOrganizerEventsUseCase
) {
    suspend operator fun invoke(userId: String): Boolean {
        if (userId.isBlank()) return false

        return try {
            // Get user to check if they are an organizer
            val user = userRepository.getUserById(userId)
            
            // Suspend the user first
            val suspendSuccess = userRepository.suspendUser(userId)
            
            if (suspendSuccess && user != null && user.role == UserRole.ORGANIZER) {
                // If user is an organizer, cancel all their events
                Log.d("SuspendUserUseCase", "Suspending organizer $userId, cancelling all events")
                val cancelSuccess = cancelAllOrganizerEventsUseCase(userId)
                if (!cancelSuccess) {
                    Log.w("SuspendUserUseCase", "User suspended but failed to cancel some events for organizer: $userId")
                }
            }
            
            suspendSuccess
        } catch (e: Exception) {
            Log.e("SuspendUserUseCase", "Error suspending user: ${e.message}", e)
            false
        }
    }
}