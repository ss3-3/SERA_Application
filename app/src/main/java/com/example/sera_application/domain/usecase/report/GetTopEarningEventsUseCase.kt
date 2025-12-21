package com.example.sera_application.domain.usecase.report

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.uimodel.TopEarningEventUiModel
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTopEarningEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val paymentRepository: PaymentRepository
) {
    operator fun invoke(limit: Int = 3): Flow<List<TopEarningEventUiModel>> = flow {
        if (limit <= 0) {
            emit(emptyList())
            return@flow
        }

        try {
            val topRevenueEvents = paymentRepository.getTopRevenueEventIds(limit)
            val allEvents = eventRepository.getAllEvents()
            val eventsMap = allEvents.associateBy { it.eventId }

            val topEarningEvents = topRevenueEvents.mapIndexed { index, eventRevenue ->
                val event = eventsMap[eventRevenue.eventId]
                TopEarningEventUiModel(
                    eventId = eventRevenue.eventId,
                    name = event?.name ?: "Unknown Event",
                    rank = index + 1,
                    imagePath = event?.imagePath,
                    revenue = eventRevenue.revenue
                )
            }

            emit(topEarningEvents)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}