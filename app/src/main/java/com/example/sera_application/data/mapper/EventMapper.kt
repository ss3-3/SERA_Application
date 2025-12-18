package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.EventEntity
import com.example.sera_application.domain.model.Event

/**
 * Interface for Event mapping operations
 * Defines contract for converting between Entity (database) and Domain (business logic)
 */
interface EventMapper {

    /**
     * Convert EventEntity (database) to Event (domain model)
     * @param entity The event entity from database
     * @param organizerName Optional organizer name (may need to be fetched separately)
     * @return Event domain model
     */
    fun toDomain(entity: EventEntity, organizerName: String = ""): Event

    /**
     * Convert Event (domain model) to EventEntity (database)
     * @param domain The event domain model
     * @return EventEntity for database storage
     */
    fun toEntity(domain: Event): EventEntity

    /**
     * Convert list of EventEntity to list of Event
     * @param entities List of event entities from database
     * @param organizerNameProvider Function to get organizer name by organizerId
     * @return List of event domain models
     */
    fun toDomainList(
        entities: List<EventEntity>,
        organizerNameProvider: (String) -> String = { "" }
    ): List<Event>

    /**
     * Convert list of Event to list of EventEntity
     * @param domains List of event domain models
     * @return List of event entities for database storage
     */
    fun toEntityList(domains: List<Event>): List<EventEntity>
}