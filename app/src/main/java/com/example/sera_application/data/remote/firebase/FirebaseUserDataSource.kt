package com.example.sera_application.data.remote.firebase

import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource(
    private val firestore: FirebaseFirestore
) : UserRemoteDataSource {

    private val usersRef = firestore.collection("users")

    override suspend fun getUserProfile(userId: String): User? {
        val document = usersRef.document(userId).get().await()
        return if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    }

    override suspend fun updateUserProfile(user: User) {
        usersRef.document(user.userId)
            .set(user)
            .await()
    }

    override suspend fun getAllUsers(): List<User> {
        val snapshot = usersRef.get().await()
        return snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    override suspend fun getPendingOrganizers(): List<User> {
        val snapshot = usersRef
            .whereEqualTo("role", "ORGANIZER")
            .whereEqualTo("isApproved", false)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    override suspend fun updateApprovalStatus(userId: String, isApproved: Boolean) {
        usersRef.document(userId)
            .update("isApproved", isApproved)
            .await()
        if (isApproved) {
            usersRef.document(userId)
                .update("accountStatus", "ACTIVE")
                .await()
        } else {
            usersRef.document(userId)
                .update("accountStatus", "REJECTED")
                .await()
        }
    }

    override suspend fun deleteUser(userId: String) {
        usersRef.document(userId).delete().await()
    }
}
