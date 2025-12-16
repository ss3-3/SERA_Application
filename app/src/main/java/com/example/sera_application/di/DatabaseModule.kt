package com.example.sera_application.di

import com.google.firebase.sessions.dagger.Module
import android.content.Context
import androidx.room.Room
import com.example.sera_application.data.local.AppDatabase
import com.example.sera_application.data.local.dao.*
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java
//
//@Module
//@InstallIn(SingletonComponent::class)
//object DatabaseModule {
//
//    @Provides
//    @Singleton
//    fun provideAppDatabase(
//        @ApplicationContext context: Context
//    ): AppDatabase {
//        return Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            "sera_database"
//        )
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    @Provides
//    fun provideUserDao(database: AppDatabase): UserDao {
//        return database.userDao()
//    }
//
//    @Provides
//    fun provideEventDao(database: AppDatabase): EventDao {
//        return database.eventDao()
//    }
//
//    @Provides
//    fun provideReservationDao(database: AppDatabase): ReservationDao {
//        return database.reservationDao()
//    }
//
//    @Provides
//    fun providePaymentDao(database: AppDatabase): PaymentDao {
//        return database.paymentDao()
//    }
//}