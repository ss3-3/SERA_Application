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

    override suspend fun createReservation(reservation: EventReservation): String? {
        val reservationId = remoteDataSource.createReservation(reservation)
        
        // Cache locally
        val createdReservation = reservation.copy(reservationId = reservationId)
        reservationDao.insertReservation(mapper.toEntity(createdReservation))
        
        return reservationId.ifEmpty { null }
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

    override suspend fun getAllReservations(): List<EventReservation> {
        return try {
            val remoteReservations = remoteDataSource.getAllReservations()
            // Cache locally
            if (remoteReservations.isNotEmpty()) {
                reservationDao.insertReservations(mapper.toEntityList(remoteReservations))
            }
            remoteReservations
        } catch (e: Exception) {
            // No direct all method in DAO yet, maybe fetch all or filter?
            // For now return empty list or implement getAll in DAO
            emptyList() 
        }
    }

    override suspend fun getReservationById(reservationId: String): EventReservation? {
        return try {
            // Try local first
            val localReservation = reservationDao.getReservationById(reservationId)
            if (localReservation != null) {
                 return mapper.toDomain(localReservation)
            }
            
            // Try remote
            val remoteReservation = remoteDataSource.getReservationById(reservationId)
            if (remoteReservation != null) {
                // Cache it
                reservationDao.insertReservation(mapper.toEntity(remoteReservation))
                remoteReservation
            } else {
                null
            }
        } catch (e: Exception) {
            null
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
                remoteDataSource.updateReservationStatus(reservationId, status)
                
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