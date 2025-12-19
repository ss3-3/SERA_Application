package com.example.sera_application.domain.usecase.event

import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.repository.EventRepository
import javax.inject.Inject
//
//class GetEventListUseCase @Inject constructor(
//    private val eventRepository: EventRepository
//) {
//    suspend operator fun invoke(): List<Event> {
//        return try {
//            eventRepository.getEventList()
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
//}

class GetEventListUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(): List<Event> {
        return eventRepository.syncEventsFromFirebase()
    }
}