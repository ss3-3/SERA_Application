package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventStatus
import javax.inject.Inject

/**
 * Implementation of EventMapper
 * Handles conversion between EventEntity and Event
 *
 * Key conversions:
 * - String ↔ EventStatus enum
 * - Int ↔ Double (prices)
 * - Int ↔ String (duration)
 */
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
            duration = "${entity.duration} day(s)",
            location = entity.location,
            rockZoneSeats = entity.rockZoneSeats,
            normalZoneSeats = entity.normalZoneSeats,
            totalSeats = entity.totalSeats,
            availableSeats = entity.availableSeats,
            rockZonePrice = entity.rockZonePrice.toDouble(),
            normalZonePrice = entity.normalZonePrice.toDouble(),
            imagePath = entity.imagePath,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override fun toEntity(domain: Event): EventEntity {
        // Parse duration string to int (e.g., "2 day(s)" -> 2)
        val durationInt = domain.duration
            .replace(" day(s)", "")
            .replace(" days", "")
            .replace(" day", "")
            .toIntOrNull() ?: 1

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
            duration = durationInt,
            location = domain.location,
            rockZoneSeats = domain.rockZoneSeats,
            normalZoneSeats = domain.normalZoneSeats,
            totalSeats = domain.totalSeats,
            availableSeats = domain.availableSeats,
            rockZonePrice = domain.rockZonePrice.toInt(),
            normalZonePrice = domain.normalZonePrice.toInt(),
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