package com.example.sera_application.data.repository
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import com.example.sera_application.data.local.dao.NotificationDao
//import com.example.sera_application.data.mapper.NotificationMapperImpl
//import com.example.sera_application.data.remote.datasource.NotificationRemoteDataSource
//import com.example.sera_application.domain.model.Notification
//import com.example.sera_application.domain.model.enums.NotificationType
//import com.example.sera_application.domain.repository.NotificationRepository
//import kotlinx.coroutines.flow.first
//import java.time.LocalDateTime
//import javax.inject.Inject
//import kotlin.collections.isNotEmpty
//
///**
// * Implementation of NotificationRepository.
// * Coordinates notification operations between remote datasource, local database, and domain layer.
// */
//class NotificationRepositoryImpl @Inject constructor(
//    private val remoteDataSource: NotificationRemoteDataSource,
//    private val notificationDao: NotificationDao
//) : NotificationRepository {
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override suspend fun sendNotification(
//        userId: String,
//        title: String,
//        message: String,
//        type: NotificationType,
//        referenceId: String?
//    ): Result<Unit> {
//        return try {
//            val notification = Notification(
//                id = "",
//                userId = userId,
//                title = title,
//                message = message,
//                type = type,
//                relatedEventId = if (type == NotificationType.EVENT_UPDATE) referenceId else null,
//                relatedPaymentId = if (type == NotificationType.PAYMENT_UPDATE) referenceId else null,
//                relatedReservationId = if (type == NotificationType.RESERVATION_UPDATE) referenceId else null,
//                isRead = false,
//                createdAt = LocalDateTime.now()
//            )
//
//            val notificationId = remoteDataSource.sendNotification(notification)
//
//            // Persist locally
//            val savedNotification = notification.copy(id = notificationId)
//            notificationDao.insertNotification(NotificationMapperImpl.toEntity(savedNotification))
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override suspend fun getNotificationsByUser(userId: String): Result<List<Notification>> {
//        return try {
//            val remoteNotifications = remoteDataSource.getNotificationsByUser(userId)
//
//            // Cache locally
//            if (remoteNotifications.isNotEmpty()) {
//                notificationDao.insertNotifications(NotificationMapperImpl.toEntityList(remoteNotifications))
//            }
//
//            Result.success(remoteNotifications)
//        } catch (e: Exception) {
//            // Fallback to local cache
//            try {
//                val localEntitiesFlow = notificationDao.getNotificationsByUserId(userId)
//                val localEntities = localEntitiesFlow.first()
//                val domainNotifications = NotificationMapperImpl.toDomainList(localEntities)
//                Result.success(domainNotifications)
//            } catch (localException: Exception) {
//                Result.failure(e)
//            }
//        }
//    }
//
//    override suspend fun markAsRead(notificationId: String): Result<Unit> {
//        return try {
//            remoteDataSource.markNotificationAsRead(notificationId)
//            notificationDao.markAsRead(notificationId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    override suspend fun clearAll(userId: String): Result<Unit> {
//        return try {
//            notificationDao.deleteAllNotifications(userId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}