package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.PaymentRepository
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FilterEventsByDateUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(startDate: Long?, endDate: Long?): Flow<List<EventListUiModel>> = flow {
        try {
            if (startDate == null || endDate == null) {
                emit(emptyList())
                return@flow
            }

            if (startDate > endDate) {
                emit(emptyList())
                return@flow
            }

            val events = eventRepository.getEventsByDateRange(startDate, endDate)

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