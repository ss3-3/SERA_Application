package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.ReservationStatistics
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class GetReservationStatisticsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(eventId: String): Result<ReservationStatistics> {
        return try {
            val reservations = reservationRepository.getEventReservations(eventId)
            
            val total = reservations.size
            val confirmed = reservations.count { it.status == ReservationStatus.CONFIRMED }
            val cancelled = reservations.count { it.status == ReservationStatus.CANCELLED }
            val pending = reservations.count { it.status == ReservationStatus.PENDING }
            
            val totalRevenue = reservations
                .filter { it.status != ReservationStatus.CANCELLED }
                .sumOf { it.totalPrice }
            
            val totalSeats = reservations
                .filter { it.status != ReservationStatus.CANCELLED }
                .sumOf { it.quantity }
            
            val avgSeats = if (total > 0) {
                totalSeats.toDouble() / total
            } else {
                0.0
            }
            
            val cancellationRate = if (total > 0) {
                (cancelled.toDouble() / total) * 100
            } else {
                0.0
            }
            
            val statistics = ReservationStatistics(
                totalReservations = total,
                confirmedReservations = confirmed,
                cancelledReservations = cancelled,
                pendingReservations = pending,
                totalRevenue = totalRevenue,
                totalSeatsReserved = totalSeats,
                averageSeatsPerReservation = avgSeats,
                cancellationRate = cancellationRate
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
