package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetOrganizerEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(organizerId: String): Flow<List<EventListUiModel>> = flow {
        if (organizerId.isBlank()) {
            emit(emptyList())
            return@flow
        }

        try {
            val events = eventRepository.getEventsByOrganizer(organizerId)
            val eventIds = events.map { it.eventId }

            val participantsMap = eventIds.associateWith { eventId ->
                reservationRepository.getParticipantsByEvent(eventId)
            }

            val revenueMap = eventIds.associateWith { eventId ->
                paymentRepository.getTotalRevenueByEvents(listOf(eventId))
            }

            val uiModels = events.map { event ->
                EventListUiModel(
                    title = event.name,
                    picture = event.imagePath ?: "",
                    organizer = event.organizerName,
                    description = event.description,
                    revenue = revenueMap[event.eventId] ?: 0.0,
                    participants = participantsMap[event.eventId] ?: 0
                )
            }

            emit(uiModels)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}