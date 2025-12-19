package com.example.sera_application.data.repository

import android.util.Log
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
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val userId = authRemoteDataSource.login(email, password)

            // check for email validation
            val isVerified = isEmailVerified()
            if (!isVerified) {
                firebaseAuth.signOut()
                return Result.failure(Exception("Please verify your email before logging in. Check your inbox for the verification link."))
            }

            val user = userRemoteDataSource.getUserProfile(userId) ?: run {
                Log.w("AuthRepository", "User profile not found for $userId, creating default profile")

                val firebaseUser = firebaseAuth.currentUser

                val defaultUser = User(
                    userId = userId,
                    fullName = firebaseUser?.displayName ?: "User",
                    email = firebaseUser?.email ?: email,
                    role = UserRole.PARTICIPANT,
                    accountStatus = "ACTIVE",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                userRemoteDataSource.updateUserProfile(defaultUser)
                defaultUser
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

            val newUser = User(
                userId = userId,
                fullName = fullName,
                email = email,
                role = try {
                    UserRole.valueOf(role.uppercase())
                } catch(e: Exception) {
                    UserRole.PARTICIPANT
                },
                accountStatus = "PENDING_VERIFICATION",
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
}