package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventStatus
import javax.inject.Inject

class EventMapperImpl @Inject constructor() : EventMapper {

    override fun toDomain(entity: EventEntity, organizerName: String): Event {
        return Event(
            eventId = entity.eventId,
            name = entity.eventName,
            organizerId = entity.organizerId,
            organizerName = organizerName.ifBlank { "" },
            description = entity.description,
            category = entity.category,
            status = try {
                EventStatus.valueOf(entity.status)
            } catch (e: IllegalArgumentException) {
                EventStatus.PENDING
            },
            date = entity.date,
            startTime = entity.startTime,
            endTime = entity.endTime,
            location = entity.location,
            rockZoneSeats = entity.rockZoneSeats,
            normalZoneSeats = entity.normalZoneSeats,
            totalSeats = entity.totalSeats,
            availableSeats = entity.availableSeats,
            rockZonePrice = entity.rockZonePrice,
            normalZonePrice = entity.normalZonePrice,
            imagePath = entity.imagePath,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun toEntity(domain: Event): EventEntity {

        return EventEntity(
            eventId = domain.eventId,
            eventName = domain.name,
            organizerId = domain.organizerId,
            description = domain.description,
            category = domain.category,
            status = domain.status.name,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            date = domain.date,
            startTime = domain.startTime,
            endTime = domain.endTime,
            location = domain.location,
            rockZoneSeats = domain.rockZoneSeats,
            normalZoneSeats = domain.normalZoneSeats,
            totalSeats = domain.totalSeats,
            availableSeats = domain.availableSeats,
            rockZonePrice = domain.rockZonePrice,
            normalZonePrice = domain.normalZonePrice,
            imagePath = domain.imagePath
        )
    }

    override fun toDomainList(
        entities: List<EventEntity>,
        organizerNameProvider: (String) -> String
    ): List<Event> {
        return entities.map { entity ->
            toDomain(entity, organizerNameProvider(entity.organizerId))
        }
    }

    override fun toEntityList(domains: List<Event>): List<EventEntity> {
        return domains.map { toEntity(it) }
    }
}