package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class OrganizerStats(
    val eventCount: Int,
    val totalRevenue: Double,
    val totalParticipants: Int,
    val averageRevenue: Double
)

class GetOrganizerStatsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(organizerId: String): Flow<OrganizerStats> = flow {
        if (organizerId.isBlank()) {
            emit(OrganizerStats(0, 0.0, 0, 0.0))
            return@flow
        }

        try {
            val events = eventRepository.getEventsByOrganizer(organizerId)
            val eventCount = events.size
            val eventIds = events.map { it.eventId }

            val totalRevenue = paymentRepository.getTotalRevenueByEvents(eventIds)
            val totalParticipants = reservationRepository.getTotalParticipantsByEvents(eventIds)
            val averageRevenue = if (eventCount > 0) totalRevenue / eventCount else 0.0

            emit(OrganizerStats(
                eventCount = eventCount,
                totalRevenue = totalRevenue,
                totalParticipants = totalParticipants,
                averageRevenue = averageRevenue
            ))
        } catch (e: Exception) {
            emit(OrganizerStats(0, 0.0, 0, 0.0))
        }
    }
}