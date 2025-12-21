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
        val snapshot = notificationsRef
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toNotification() }
    }

    override suspend fun markNotificationAsRead(notificationId: String) {
        notificationsRef.document(notificationId)
            .update("isRead", true)
            .await()
    }
}