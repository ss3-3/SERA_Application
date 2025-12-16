package com.example.sera_application.data.repository

import com.example.sera_application.data.local.dao.ReservationDao
import com.example.sera_application.data.mapper.ReservationMapper
import com.example.sera_application.data.remote.datasource.ReservationRemoteDataSource
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

/**
 * Implementation of ReservationRepository.
 * Coordinates reservation operations between remote datasource, local database, and domain layer.
 */
class ReservationRepositoryImpl @Inject constructor(
    private val remoteDataSource: ReservationRemoteDataSource,
    private val reservationDao: ReservationDao,
    private val mapper: ReservationMapper
) : ReservationRepository {

    override suspend fun createReservation(reservation: EventReservation): Boolean {
        return try {
            val reservationId = remoteDataSource.createReservation(reservation)
            
            // Cache locally
            val createdReservation = reservation.copy(reservationId = reservationId)
            reservationDao.insertReservation(mapper.toEntity(createdReservation))
            
            reservationId.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun cancelReservation(reservationId: String): Boolean {
        return try {
            remoteDataSource.cancelReservation(reservationId)
            
            // Update local cache
            reservationDao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED.name)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserReservations(userId: String): List<EventReservation> {
        return try {
            val remoteReservations = remoteDataSource.getReservationsByUser(userId)
            
            // Cache locally
            if (remoteReservations.isNotEmpty()) {
                reservationDao.insertReservations(mapper.toEntityList(remoteReservations))
            }
            
            remoteReservations
        } catch (e: Exception) {
            // Fallback to local cache
            val localEntities = reservationDao.getReservationsByUser(userId)
            mapper.toDomainList(localEntities)
        }
    }

    override suspend fun getEventReservations(eventId: String): List<EventReservation> {
        return try {
            val remoteReservations = remoteDataSource.getReservationsByEvent(eventId)
            
            // Cache locally
            if (remoteReservations.isNotEmpty()) {
                reservationDao.insertReservations(mapper.toEntityList(remoteReservations))
            }
            
            remoteReservations
        } catch (e: Exception) {
            // Fallback to local cache
            val localEntities = reservationDao.getReservationsByEvent(eventId)
            mapper.toDomainList(localEntities)
        }
    }

    override suspend fun updateReservationStatus(reservationId: String, status: String): Boolean {
        return try {
            val reservation = reservationDao.getReservationById(reservationId)
            if (reservation != null) {
                // Update remote if exists
                val updatedReservation = mapper.toDomain(reservation).copy(
                    status = try {
                        ReservationStatus.valueOf(status)
                    } catch (e: IllegalArgumentException) {
                        ReservationStatus.PENDING
                    }
                )
                remoteDataSource.cancelReservation(reservationId)
                
                // Update local
                reservationDao.updateReservationStatus(reservationId, status)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}