package com.example.sera_application.data.remote.firebase

import com.example.sera_application.data.mapper.NotificationMapper
import com.example.sera_application.data.mapper.toNotification
import com.example.sera_application.data.remote.datasource.NotificationRemoteDataSource
import com.example.sera_application.domain.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseNotificationDataSource(
    private val firestore: FirebaseFirestore
) : NotificationRemoteDataSource {

    private val notificationsRef = firestore.collection("notifications")

    override suspend fun sendNotification(notification: Notification): String {
        val docRef = if (notification.id.isBlank()) {
            notificationsRef.document()
        } else {
            notificationsRef.document(notification.id)
        }
        val notificationWithId = notification.copy(id = docRef.id)
        val notificationMap = NotificationMapper.notificationToFirestoreMap(notificationWithId)
        docRef.set(notificationMap).await()
        return docRef.id
    }

    override suspend fun getNotificationsByUser(userId: String): List<Notification> {
        return try {
            val snapshot = notificationsRef
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toNotification() }
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                android.util.Log.e("FirebaseNotificationDataSource", "Permission denied when fetching notifications for user: $userId")
                throw Exception("Permission denied: Unable to access notifications. Please check your account permissions.")
            } else if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                android.util.Log.e("FirebaseNotificationDataSource", "Missing Firestore index for notifications query: ${e.message}", e)
                // Extract the index creation link from the error message if available
                val errorMessage = e.message ?: "Missing Firestore index"
                throw Exception("Missing Firestore index. Please create the required index:\n\n$errorMessage\n\nCheck the Firebase Console → Firestore → Indexes tab to create it.")
            } else {
                android.util.Log.e("FirebaseNotificationDataSource", "Error fetching notifications: ${e.message}", e)
                throw Exception("Failed to load notifications: ${e.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseNotificationDataSource", "Unexpected error fetching notifications: ${e.message}", e)
            throw Exception("Failed to load notifications: ${e.message}")
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String) {
        notificationsRef.document(notificationId)
            .update("isRead", true)
            .await()
    }
}