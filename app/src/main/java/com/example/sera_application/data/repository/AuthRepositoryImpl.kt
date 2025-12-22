package com.example.sera_application.data.repository

import android.util.Log
import com.example.sera_application.data.remote.api.EmailRequest
import com.example.sera_application.data.remote.api.EmailService
import com.example.sera_application.data.remote.datasource.AuthRemoteDataSource
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val emailService: EmailService
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val userId = authRemoteDataSource.login(email, password)

            // Check Firebase Auth email verification
            val isFirebaseEmailVerified = isEmailVerified()
            if (!isFirebaseEmailVerified) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Please verify your email before logging in. Check your inbox for the verification link."))
            }

            // Get user profile from Firestore - force fresh fetch (bypass cache)
            // Use source = Source.SERVER to ensure we get latest data from Firestore
            val user = userRemoteDataSource.getUserProfile(userId) ?: run {
                Log.w("AuthRepository", "User profile not found for $userId, creating default profile")

                val firebaseUser = firebaseAuth.currentUser

                val defaultUser = User(
                    userId = userId,
                    fullName = firebaseUser?.displayName ?: "User",
                    email = firebaseUser?.email ?: email,
                    role = UserRole.PARTICIPANT,
                    accountStatus = "ACTIVE",
                    emailVerified = isFirebaseEmailVerified,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                userRemoteDataSource.updateUserProfile(defaultUser)
                defaultUser
            }
            
            // Log full user data for debugging
            Log.d("AuthRepository", "User fetched from Firestore - userId: ${user.userId}, role: ${user.role}, approvalStatus: ${user.approvalStatus}, isApproved: ${user.isApproved}, accountStatus: ${user.accountStatus}")

            // Sync Firebase Auth email verification status to Firestore if different
            if (user.emailVerified != isFirebaseEmailVerified) {
                val updatedUser = user.copy(
                    emailVerified = isFirebaseEmailVerified,
                    updatedAt = System.currentTimeMillis()
                )
                userRemoteDataSource.updateUserProfile(updatedUser)
            }

            // For organizers, check both email verification AND approval status
            if (user.role == UserRole.ORGANIZER) {
                // Check email verification
                if (!user.emailVerified) {
                    firebaseAuth.signOut()
                    return Result.failure(Exception("Please verify your email before logging in. Check your inbox for the verification link."))
                }
                
                // Check approval status - fetch fresh data from Firestore to ensure we have latest approval status
                val approvalStatus = user.approvalStatus
                Log.d("AuthRepository", "Organizer login check - userId: $userId, approvalStatus: $approvalStatus, isApproved: ${user.isApproved}, accountStatus: ${user.accountStatus}")
                
                // Check if approved: either approvalStatus == APPROVED OR (isApproved == true AND accountStatus == ACTIVE)
                val isApproved = approvalStatus == com.example.sera_application.domain.model.enums.ApprovalStatus.APPROVED 
                    || (user.isApproved && user.accountStatus == "ACTIVE" && approvalStatus != com.example.sera_application.domain.model.enums.ApprovalStatus.REJECTED)
                
                if (!isApproved) {
                    firebaseAuth.signOut()
                    val message = if (approvalStatus == com.example.sera_application.domain.model.enums.ApprovalStatus.PENDING) {
                        "Your organizer account is pending admin approval. Please wait for an administrator to approve your account. You will receive an email notification once approved."
                    } else if (approvalStatus == com.example.sera_application.domain.model.enums.ApprovalStatus.REJECTED) {
                        "Your organizer account has been rejected. Please contact an administrator."
                    } else {
                        "Your organizer account approval status: ${approvalStatus?.name ?: "PENDING"}. Please contact an administrator."
                    }
                    Log.w("AuthRepository", "Organizer login rejected - $message (approvalStatus: $approvalStatus, isApproved: ${user.isApproved}, accountStatus: ${user.accountStatus})")
                    return Result.failure(Exception(message))
                }
                
                Log.d("AuthRepository", "Organizer login approved - userId: $userId")
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        role: String
    ): Result<User> {
        return try {
            val userId = authRemoteDataSource.register(email, password, fullName, role)

            val userRole = try {
                UserRole.valueOf(role.uppercase())
            } catch(e: Exception) {
                UserRole.PARTICIPANT
            }
            
            val isApproved = when (userRole) {
                UserRole.PARTICIPANT -> true
                UserRole.ORGANIZER -> false
                UserRole.ADMIN -> true
            }
            
            val accountStatus = when (userRole) {
                UserRole.PARTICIPANT -> "ACTIVE"
                UserRole.ORGANIZER -> "PENDING"
                UserRole.ADMIN -> "ACTIVE"
            }
            
            // Set approval status for organizers
            val approvalStatus = when (userRole) {
                UserRole.ORGANIZER -> com.example.sera_application.domain.model.enums.ApprovalStatus.PENDING
                else -> null
            }
            
            val newUser = User(
                userId = userId,
                fullName = fullName,
                email = email,
                role = userRole,
                accountStatus = accountStatus,
                isApproved = isApproved,
                emailVerified = false, // Email not verified yet
                approvalStatus = approvalStatus,
                approvedAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // store to Firestore
            userRemoteDataSource.updateUserProfile(newUser)

            // send validation email
            val emailSent = sendEmailVerification()
            if (emailSent.isFailure) {
                Log.w("AuthRepository", "Failed to send verification email", emailSent.exceptionOrNull())
            }

            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Boolean> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return Result.failure(Exception("No user is currently signed in"))
            }

            if (user.isEmailVerified) {
                return Result.success(true)
            }

            user.sendEmailVerification().await()
            Log.d("AuthRepository", "Verification email sent to ${user.email}")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to send verification email", e)
            Result.failure(e)
        }
    }


    override suspend fun isEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser
        return user?.isEmailVerified ?: false
    }

    override suspend fun reloadUser(): Result<Boolean> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return Result.failure(Exception("No user is currently signed in"))
            }

            user.reload().await()
            Result.success(user.isEmailVerified)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to reload user", e)
            Result.failure(e)
        }
    }

    override suspend fun logout(): Boolean {
        return try {
            authRemoteDataSource.logout()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout failed", e)
            false
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userId = authRemoteDataSource.getCurrentUserId()
            userId?.let { userRemoteDataSource.getUserProfile(it) }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Get current user failed", e)
            null
        }
    }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            val email = user.email ?: return false

            // revalidation
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            user.updatePassword(newPassword).await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Update password failed", e)
            false
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d("AuthRepository", "Password reset email sent to $email")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to send password reset email", e)
            val errorMessage = when {
                e.message?.contains("no user record") == true ->
                    "No account found with this email address"
                e.message?.contains("badly formatted") == true ->
                    "Invalid email format"
                else -> e.message ?: "Failed to send password reset email"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun sendOrganizerApprovalEmail(email: String, fullName: String): Result<Boolean> {
        return try {
            // TODO: Move API key to BuildConfig for production
            // Get your API key from: https://resend.com/api-keys
            val resendApiKey = "re_6nzGu7vb_AESwGZqvaNy3ECaMiUMxEytU"
            
            Log.d("AuthRepository", "Attempting to send approval email to: $email for user: $fullName")
            
            val emailRequest = EmailRequest(
                from = "SERA App <onboarding@resend.dev>",
                to = email,
                subject = "Organizer Account Approved",
                html = """
                    <h2>Your Organizer Account Has Been Approved!</h2>
                    <p>Hello $fullName,</p>
                    <p>Great news! Your organizer account has been approved by an administrator.</p>
                    <p>You can now log in to the SERA Application and start creating events.</p>
                    <p>Thank you for your patience.</p>
                    <br>
                    <p>Best regards,<br>SERA Team</p>
                """.trimIndent()
            )
            
            val response = emailService.sendEmail(
                apiKey = "Bearer $resendApiKey",
                emailRequest = emailRequest
            )
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("AuthRepository", "Approval email sent successfully to $email. Response: ${responseBody?.id ?: "No ID"}")
                Result.success(true)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Unknown error"
                } catch (e: Exception) {
                    "Could not read error body: ${e.message}"
                }
                Log.e("AuthRepository", "Failed to send email to $email. Code: ${response.code()}, Message: ${response.message()}, Body: $errorBody")
                Result.failure(Exception("Failed to send email: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception sending approval email to $email: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Boolean> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
            
            // 1. Delete Firestore user data
            userRemoteDataSource.deleteUser(userId)
            
            // 2. Delete Firebase Auth account
            authRemoteDataSource.deleteAccount()
            
            // Note: Event cancellation is handled in DeleteAccountUseCase before calling this method
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Delete account failed", e)
            Result.failure(e)
        }
    }
}