package com.example.sera_application.data.remote.firebase

import android.util.Log
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.model.enums.ApprovalStatus
import com.example.sera_application.domain.model.enums.UserRole
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource(
    private val firestore: FirebaseFirestore
) : UserRemoteDataSource {

    private val usersRef = firestore.collection("users")

    override suspend fun getUserProfile(userId: String): User? {
        // Use Source.SERVER to force fetch from Firestore (bypass cache)
        // This ensures we get the latest approval status
        val document = usersRef.document(userId).get(Source.SERVER).await()
        return if (document.exists()) {
            val user = document.toUser()
            Log.d("FirebaseUserDataSource", "getUserProfile - userId: $userId, approvalStatus: ${user?.approvalStatus}, isApproved: ${user?.isApproved}")
            user
        } else {
            null
        }
    }

    override suspend fun updateUserProfile(user: User) {
        // Convert User to Map with enums as Strings for Firestore
        val userMap = mutableMapOf<String, Any?>(
            "userId" to user.userId,
            "fullName" to user.fullName,
            "email" to user.email,
            "phone" to user.phone,
            "role" to user.role.name, // Convert enum to String
            "profileImagePath" to user.profileImagePath,
            "accountStatus" to user.accountStatus,
            "isApproved" to user.isApproved,
            "emailVerified" to user.emailVerified,
            "approvalStatus" to user.approvalStatus?.name, // Convert enum to String
            "approvedAt" to user.approvedAt,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt
        )
        
        usersRef.document(user.userId)
            .set(userMap)
            .await()
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            Log.d("FirebaseUserDataSource", "=== Getting all users from Firebase ===")
            val snapshot = usersRef.get().await()
            Log.d("FirebaseUserDataSource", "Firebase returned ${snapshot.documents.size} user documents")
            
            val users = snapshot.documents.mapNotNull { doc ->
                val user = doc.toUser()
                if (user != null) {
                    Log.d("FirebaseUserDataSource", "Mapped user: ${user.userId}, ${user.email}, role=${user.role}")
                } else {
                    Log.w("FirebaseUserDataSource", "Failed to map document: ${doc.id}")
                }
                user
            }
            
            Log.d("FirebaseUserDataSource", "Successfully mapped ${users.size} users")
            users
        } catch (e: Exception) {
            Log.e("FirebaseUserDataSource", "Error getting all users from Firebase: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getPendingOrganizers(): List<User> {
        val snapshot = usersRef
            .whereEqualTo("role", "ORGANIZER")
            .whereEqualTo("approvalStatus", "PENDING")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toUser() }
    }
    
    /**
     * Convert Firestore DocumentSnapshot to User domain model
     * Handles enum deserialization from String
     */
    private fun DocumentSnapshot.toUser(): User? {
        return try {
            val data = this.data ?: return null
            
            User(
                userId = data["userId"]?.toString() ?: this.id,
                fullName = data["fullName"]?.toString() ?: "",
                email = data["email"]?.toString() ?: "",
                phone = data["phone"]?.toString(),
                role = try {
                    val roleStr = data["role"]?.toString() ?: "PARTICIPANT"
                    UserRole.valueOf(roleStr)
                } catch (e: Exception) {
                    Log.w("FirebaseUserDataSource", "Invalid role: ${data["role"]}, defaulting to PARTICIPANT")
                    UserRole.PARTICIPANT
                },
                profileImagePath = data["profileImagePath"]?.toString(),
                accountStatus = data["accountStatus"]?.toString() ?: "ACTIVE",
                isApproved = data["isApproved"] as? Boolean ?: true,
                emailVerified = data["emailVerified"] as? Boolean ?: false,
                approvalStatus = data["approvalStatus"]?.toString()?.let { statusStr ->
                    try {
                        ApprovalStatus.valueOf(statusStr.uppercase()) // Case-insensitive parsing
                    } catch (e: Exception) {
                        Log.w("FirebaseUserDataSource", "Invalid approvalStatus: '$statusStr', error: ${e.message}")
                        null
                    }
                } ?: run {
                    // If approvalStatus is null, check isApproved to infer status
                    val isApproved = data["isApproved"] as? Boolean ?: true
                    if (!isApproved) {
                        Log.w("FirebaseUserDataSource", "approvalStatus is null but isApproved is false, inferring PENDING")
                        ApprovalStatus.PENDING
                    } else {
                        null
                    }
                },
                approvedAt = (data["approvedAt"] as? Number)?.toLong(),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("FirebaseUserDataSource", "Error converting document to User: ${e.message}", e)
            null
        }
    }

    override suspend fun updateApprovalStatus(userId: String, isApproved: Boolean) {
        val updates = mutableMapOf<String, Any>(
            "isApproved" to isApproved,
            "updatedAt" to System.currentTimeMillis()
        )
        
        if (isApproved) {
            updates["accountStatus"] = "ACTIVE"
            updates["approvalStatus"] = "APPROVED"
            updates["approvedAt"] = System.currentTimeMillis()
            
            usersRef.document(userId)
                .update(updates)
                .await()
        } else {
            updates["accountStatus"] = "REJECTED"
            updates["approvalStatus"] = "REJECTED"
            // Use FieldValue.delete() to remove the field instead of setting to null
            updates["approvedAt"] = com.google.firebase.firestore.FieldValue.delete()
            
            usersRef.document(userId)
                .update(updates)
                .await()
        }
    }

    override suspend fun deleteUser(userId: String) {
        usersRef.document(userId).delete().await()
    }
}
