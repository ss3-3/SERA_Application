package com.example.sera_application.domain.usecase.auth

import android.util.Log
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.enums.NotificationType
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.usecase.notification.SendNotificationUseCase
import com.example.sera_application.domain.usecase.user.GetAllUsersUseCase
import javax.inject.Inject

/**
 * Use case for user registration
 * Handles account creation with validation
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        role: String = "PARTICIPANT"
    ): Result<User> {
        // Validate input
        if (fullName.isBlank()) {
            return Result.failure(Exception("Full name cannot be empty"))
        }

        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(Exception("Invalid email format"))
        }

        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        // Call repository to perform registration
        val result = authRepository.register(fullName, email, password, role)
        
        // If registration successful, notify all admins
        result.onSuccess { newUser ->
            try {
                // Get all admin users
                val allUsers = getAllUsersUseCase()
                val adminUsers = allUsers.filter { it.role == UserRole.ADMIN }
                
                // Determine role display name
                val roleDisplayName = when (newUser.role) {
                    UserRole.PARTICIPANT -> "Participant"
                    UserRole.ORGANIZER -> "Organizer"
                    UserRole.ADMIN -> "Admin"
                }
                
                // Send notification to each admin
                adminUsers.forEach { admin ->
                    try {
                        sendNotificationUseCase(
                            userId = admin.userId,
                            title = "New User Registration",
                            message = "$fullName ($email) has registered as a $roleDisplayName. Please review their account if approval is required.",
                            type = NotificationType.SYSTEM
                        )
                    } catch (e: Exception) {
                        Log.e("RegisterUseCase", "Failed to send notification to admin ${admin.userId}: ${e.message}", e)
                        // Continue with other admins even if one fails
                    }
                }
                
                if (adminUsers.isNotEmpty()) {
                    Log.d("RegisterUseCase", "Sent registration notification to ${adminUsers.size} admin(s)")
                }
            } catch (e: Exception) {
                Log.e("RegisterUseCase", "Failed to notify admins about new registration: ${e.message}", e)
                // Don't fail registration if notification fails
            }
        }
        
        return result
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}