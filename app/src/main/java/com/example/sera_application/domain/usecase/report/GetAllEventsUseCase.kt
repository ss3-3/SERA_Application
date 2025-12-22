package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(): Flow<List<EventListUiModel>> = flow {
        try {
            val events = eventRepository.getAllEvents()

            val uiModels = events.map { event ->
                val participants = reservationRepository.getParticipantsByEvent(event.eventId)
                val revenue = paymentRepository.getTotalRevenueByEvents(listOf(event.eventId))

                EventListUiModel(
                    title = event.name,
                    picture = event.imagePath ?: "",
                    organizer = event.organizerName,
                    description = event.description,
                    revenue = revenue,
                    participants = participants
                )
            }

            emit(uiModels)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}