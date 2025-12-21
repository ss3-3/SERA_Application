package com.example.sera_application.di

import com.example.sera_application.data.repository.AuthRepositoryImpl
import com.example.sera_application.data.repository.EventRepositoryImpl
import com.example.sera_application.data.repository.ImageRepositoryImpl
import com.example.sera_application.data.repository.NotificationRepositoryImpl
import com.example.sera_application.data.repository.PaymentRepositoryImpl
import com.example.sera_application.data.repository.ReportRepositoryImpl
import com.example.sera_application.data.repository.ReservationRepositoryImpl
import com.example.sera_application.data.repository.UserRepositoryImpl
import com.example.sera_application.domain.repository.AuthRepository
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.ImageRepository
import com.example.sera_application.domain.repository.NotificationRepository
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.repository.ReportRepository
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository module for dependency injection.
 * Binds repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        reservationRepositoryImpl: ReservationRepositoryImpl
    ): ReservationRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        imageRepositoryImpl: ImageRepositoryImpl
    ): ImageRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository
}