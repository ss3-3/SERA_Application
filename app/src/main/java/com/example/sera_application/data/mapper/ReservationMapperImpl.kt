package com.example.sera_application.data.mapper

import com.example.sera_application.data.local.entity.ReservationEntity
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import javax.inject.Inject

/**
 * Implementation of ReservationMapper
 * Handles conversion between ReservationEntity and EventReservation
 *
 * Key conversions:
 * - String ↔ ReservationStatus enum
 */
class ReservationMapperImpl @Inject constructor() : ReservationMapper {

    override fun toDomain(entity: ReservationEntity): EventReservation {
        return EventReservation(
            reservationId = entity.reservationId,
            eventId = entity.eventId,
            userId = entity.userId,
            seats = entity.seats,
            rockZoneSeats = entity.rockZoneSeats,
            normalZoneSeats = entity.normalZoneSeats,
            totalPrice = entity.totalPrice,
            status = try {
                ReservationStatus.valueOf(entity.status)
            } catch (e: IllegalArgumentException) {
                ReservationStatus.PENDING
            },
            createdAt = entity.createdAt
        )
    }

    override fun toEntity(domain: EventReservation): ReservationEntity {
        return ReservationEntity(
            reservationId = domain.reservationId,
            eventId = domain.eventId,
            userId = domain.userId,
            seats = domain.seats,
            rockZoneSeats = domain.rockZoneSeats,
            normalZoneSeats = domain.normalZoneSeats,
            totalPrice = domain.totalPrice,
            status = domain.status.name,
            createdAt = domain.createdAt
        )
    }

    override fun toDomainList(entities: List<ReservationEntity>): List<EventReservation> {
        return entities.map { toDomain(it) }
    }

    override fun toEntityList(domains: List<EventReservation>): List<ReservationEntity> {
        return domains.map { toEntity(it) }
    }
}