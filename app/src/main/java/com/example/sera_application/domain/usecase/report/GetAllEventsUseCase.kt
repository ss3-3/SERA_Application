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
            val events = try {
                eventRepository.getAllEvents()
            } catch (e: Exception) {
                android.util.Log.e("GetAllEventsUseCase", "Error getting events: ${e.message}", e)
                emptyList()
            }

            if (events.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            val uiModels = events.mapNotNull { event ->
                try {
                    val participants = try {
                        reservationRepository.getParticipantsByEvent(event.eventId)
                    } catch (e: Exception) {
                        android.util.Log.e("GetAllEventsUseCase", "Error getting participants for event ${event.eventId}: ${e.message}", e)
                        0
                    }
                    
                    val revenue = try {
                        paymentRepository.getTotalRevenueByEvents(listOf(event.eventId))
                    } catch (e: Exception) {
                        android.util.Log.e("GetAllEventsUseCase", "Error getting revenue for event ${event.eventId}: ${e.message}", e)
                        0.0
                    }

                    EventListUiModel(
                        title = event.name,
                        picture = event.imagePath ?: "",
                        organizer = event.organizerName,
                        description = event.description,
                        revenue = revenue,
                        participants = participants
                    )
                } catch (e: Exception) {
                    android.util.Log.e("GetAllEventsUseCase", "Error creating EventListUiModel for event ${event.eventId}: ${e.message}", e)
                    null
                }
            }

            emit(uiModels)
        } catch (e: Exception) {
            android.util.Log.e("GetAllEventsUseCase", "Unexpected error in GetAllEventsUseCase: ${e.message}", e)
            e.printStackTrace()
            emit(emptyList())
        }
    }
}