package com.example.sera_application.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sera_application.data.local.dao.EventDao
import com.example.sera_application.data.local.dao.NotificationDao
import com.example.sera_application.data.local.dao.PaymentDao
import com.example.sera_application.data.local.dao.ReservationDao
import com.example.sera_application.data.local.dao.UserDao
import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.data.local.entity.NotificationEntity
import com.example.sera_application.data.local.entity.PaymentEntity
import com.example.sera_application.data.local.entity.ReservationEntity
import com.example.sera_application.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        ReservationEntity::class,
        PaymentEntity::class,
        NotificationEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun reservationDao(): ReservationDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "sera_database"
    }
}