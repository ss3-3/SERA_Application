package com.example.sera_application.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Use case module for dependency injection.
 * 
 * Note: All use cases are automatically provided by Hilt since they have
 * @Inject constructors. No explicit @Provides methods are needed.
 * 
 * Use cases included:
 * - Auth: LoginUseCase, RegisterUseCase, LogoutUseCase, GetCurrentUserUseCase
 * - User: GetUserProfileUseCase, UpdateUserProfileUseCase, GetAllUsersUseCase,
 *         ApproveOrganizerUseCase, SuspendUserUseCase
 * - Event: CreateEventUseCase, UpdateEventUseCase, DeleteEventUseCase,
 *          GetEventListUseCase, GetEventByIdUseCase, GetEventsByOrganizerUseCase,
 *          ApproveEventUseCase, RejectEventUseCase, CloseEventUseCase
 * - Reservation: CreateReservationUseCase, CancelReservationUseCase,
 *                GetUserReservationsUseCase, GetEventReservationsUseCase,
 *                UpdateReservationStatusUseCase
 * - Payment: ProcessPaymentUseCase, GetPaymentByIdUseCase, GetPaymentHistoryUseCase,
 *            ValidatePaymentUseCase, RefundPaymentUseCase
 * - Notification: SendNotificationUseCase
 * - Image: SaveImageUseCase, LoadImageUseCase, DeleteImageUseCase
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // All use cases are automatically provided via constructor injection
}