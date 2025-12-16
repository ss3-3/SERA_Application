package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.ReservationEntity
import com.example.sera_application.domain.model.EventReservation

/**
 * Interface for Reservation mapping operations
 * Defines contract for converting between Entity (database) and Domain (business logic)
 */
interface ReservationMapper {

    /**
     * Convert ReservationEntity (database) to EventReservation (domain model)
     * @param entity The reservation entity from database
     * @return EventReservation domain model
     */
    fun toDomain(entity: ReservationEntity): EventReservation

    /**
     * Convert EventReservation (domain model) to ReservationEntity (database)
     * @param domain The reservation domain model
     * @return ReservationEntity for database storage
     */
    fun toEntity(domain: EventReservation): ReservationEntity

    /**
     * Convert list of ReservationEntity to list of EventReservation
     * @param entities List of reservation entities from database
     * @return List of reservation domain models
     */
    fun toDomainList(entities: List<ReservationEntity>): List<EventReservation>

    /**
     * Convert list of EventReservation to list of ReservationEntity
     * @param domains List of reservation domain models
     * @return List of reservation entities for database storage
     */
    fun toEntityList(domains: List<EventReservation>): List<ReservationEntity>
}