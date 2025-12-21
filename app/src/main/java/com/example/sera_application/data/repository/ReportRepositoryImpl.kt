package com.example.sera_application.data.repository

import com.example.sera_application.domain.model.ReservationStatistics
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReportRepository
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository
) : ReportRepository {

    override suspend fun getReservationStatistics(eventId: String?): ReservationStatistics {
        val reservations = if (eventId != null) {
            reservationRepository.getEventReservations(eventId).first()
        } else {
            reservationRepository.getAllReservations()
        }

        val totalReservations = reservations.size
        val confirmedReservations = reservations.count { it.status == ReservationStatus.CONFIRMED }
        val cancelledReservations = reservations.count { it.status == ReservationStatus.CANCELLED }
        val pendingReservations = reservations.count { it.status == ReservationStatus.PENDING }
        
        val totalSeatsReserved = reservations.sumOf { it.seats }
        val averageSeatsPerReservation = if (totalReservations > 0) {
            totalSeatsReserved.toDouble() / totalReservations
        } else {
            0.0
        }
        
        val cancellationRate = if (totalReservations > 0) {
            (cancelledReservations.toDouble() / totalReservations) * 100.0
        } else {
            0.0
        }

        // Calculate total revenue from confirmed reservations
        val totalRevenue = reservations
            .filter { it.status == ReservationStatus.CONFIRMED }
            .sumOf { it.totalPrice }

        return ReservationStatistics(
            totalReservations = totalReservations,
            confirmedReservations = confirmedReservations,
            cancelledReservations = cancelledReservations,
            pendingReservations = pendingReservations,
            totalRevenue = totalRevenue,
            totalSeatsReserved = totalSeatsReserved,
            averageSeatsPerReservation = averageSeatsPerReservation,
            cancellationRate = cancellationRate
        )
    }

    override suspend fun getTotalRevenue(eventId: String?): Double {
        val reservations = if (eventId != null) {
            reservationRepository.getEventReservations(eventId).first()
        } else {
            reservationRepository.getAllReservations()
        }
        
        return reservations
            .filter { it.status == ReservationStatus.CONFIRMED }
            .sumOf { it.totalPrice }
    }

    override suspend fun getEventStatistics(eventId: String): ReservationStatistics {
        return getReservationStatistics(eventId)
    }

    override suspend fun getUserStatistics(userId: String): ReservationStatistics {
        val reservations = reservationRepository.getUserReservations(userId).first()

        val totalReservations = reservations.size
        val confirmedReservations = reservations.count { it.status == ReservationStatus.CONFIRMED }
        val cancelledReservations = reservations.count { it.status == ReservationStatus.CANCELLED }
        val pendingReservations = reservations.count { it.status == ReservationStatus.PENDING }
        
        val totalSeatsReserved = reservations.sumOf { it.seats }
        val averageSeatsPerReservation = if (totalReservations > 0) {
            totalSeatsReserved.toDouble() / totalReservations
        } else {
            0.0
        }
        
        val cancellationRate = if (totalReservations > 0) {
            (cancelledReservations.toDouble() / totalReservations) * 100.0
        } else {
            0.0
        }

        val totalRevenue = reservations
            .filter { it.status == ReservationStatus.CONFIRMED }
            .sumOf { it.totalPrice }

        return ReservationStatistics(
            totalReservations = totalReservations,
            confirmedReservations = confirmedReservations,
            cancelledReservations = cancelledReservations,
            pendingReservations = pendingReservations,
            totalRevenue = totalRevenue,
            totalSeatsReserved = totalSeatsReserved,
            averageSeatsPerReservation = averageSeatsPerReservation,
            cancellationRate = cancellationRate
        )
    }
}

