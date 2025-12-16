package com.example.sera_application.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sera_application.data.local.dao.EventDao
import com.example.sera_application.data.local.dao.PaymentDao
import com.example.sera_application.data.local.dao.ReservationDao
import com.example.sera_application.data.local.dao.UserDao
import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.data.local.entity.PaymentEntity
import com.example.sera_application.data.local.entity.ReservationEntity
import com.example.sera_application.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        ReservationEntity::class,
        PaymentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun reservationDao(): ReservationDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        const val DATABASE_NAME = "sera_database"
    }
}