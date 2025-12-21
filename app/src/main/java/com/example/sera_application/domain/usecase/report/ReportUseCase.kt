package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.ReservationStatistics
import com.example.sera_application.domain.model.uimodel.TopEarningEventUiModel
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.ReservationRepository
import kotlin.collections.find
import kotlin.collections.mapNotNull

class ReportUseCase(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository
) {

    suspend fun getTop3EarningEvents(): List<TopEarningEventUiModel> {
        val events = eventRepository.getAllEvents()
        val reservations = reservationRepository.getAllReservations()

        val stats = buildReservationStatistics(reservations, events)

        return buildTop3EarningEvents(events, stats)
    }
}

fun buildTop3EarningEvents(
    events: List<Event>,
    reservationStats: Map<String, ReservationStatistics>
): List<TopEarningEventUiModel> {

    return events
        .mapNotNull { event ->
            val stats = reservationStats[event.eventId] ?: return@mapNotNull null

            TopEarningEventUiModel(
                rank = 0,
                eventId = event.eventId,
                name = event.name,
                imagePath = event.imagePath,
                revenue = stats.totalRevenue
            )
        }
        .sortedByDescending { it.revenue }
        .take(3)
        .mapIndexed { index, event ->
            event.copy(rank = index + 1)
        }
}

fun buildReservationStatistics(
    reservations: List<EventReservation>,
    events: List<Event>
): Map<String, ReservationStatistics> {

    return reservations
        .groupBy { it.eventId }
        .mapValues { (_, reservationsForEvent) ->

            val totalRevenue = reservationsForEvent.sumOf { reservation ->
                val event = events.find { it.eventId == reservation.eventId }
                val price = event?.normalZonePrice ?: 0.0
                price * reservation.seats
            }

            ReservationStatistics(
                totalReservations = reservationsForEvent.size,
                confirmedReservations = reservationsForEvent.size,
                cancelledReservations = 0,
                pendingReservations = 0,
                totalRevenue = totalRevenue,
                totalSeatsReserved = reservationsForEvent.sumOf { it.seats },
                averageSeatsPerReservation =
                    reservationsForEvent.map { it.seats }.average(),
                cancellationRate = 0.0
            )
        }
}