package com.example.sera_application.data.repository

import com.example.sera_application.data.local.dao.ReservationDao
import com.example.sera_application.data.mapper.ReservationMapper
import com.example.sera_application.data.remote.datasource.ReservationRemoteDataSource
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override suspend fun createReservation(reservation: EventReservation): Result<String> {
        return try {
            val reservationId = remoteDataSource.createReservation(reservation)

            // Cache locally
            val createdReservation = reservation.copy(reservationId = reservationId)
            reservationDao.insertReservation(mapper.toEntity(createdReservation))

            if (reservationId.isNotEmpty()) {
                Result.success(reservationId)
            } else {
                Result.failure(Exception("Failed to create reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelReservation(reservationId: String): Result<Unit> {
        return try {
            remoteDataSource.cancelReservation(reservationId)

            // Update local cache
            reservationDao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserReservations(userId: String): Flow<List<EventReservation>> {
        return flow {
            try {
                val remoteReservations = remoteDataSource.getReservationsByUser(userId)

                // Cache locally
                if (remoteReservations.isNotEmpty()) {
                    reservationDao.insertReservations(mapper.toEntityList(remoteReservations))
                }

                emit(remoteReservations)
            } catch (e: Exception) {
                // Fallback to local cache
                val localEntities = reservationDao.getReservationsByUser(userId)
                emit(mapper.toDomainList(localEntities))
            }
        }
    }

    override fun getEventReservations(eventId: String): Flow<List<EventReservation>> {
        return flow {
            try {
                val remoteReservations = remoteDataSource.getReservationsByEvent(eventId)

                // Cache locally
                if (remoteReservations.isNotEmpty()) {
                    reservationDao.insertReservations(mapper.toEntityList(remoteReservations))
                }

                emit(remoteReservations)
            } catch (e: Exception) {
                // Fallback to local cache
                val localEntities = reservationDao.getReservationsByEvent(eventId)
                emit(mapper.toDomainList(localEntities))
            }
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

    override suspend fun updateReservationStatus(reservationId: String, status: ReservationStatus): Result<Unit> {
        return try {
            val reservation = reservationDao.getReservationById(reservationId)
            if (reservation != null) {
                // Update remote
                remoteDataSource.updateReservationStatus(reservationId, status.name)

                // Update local
                reservationDao.updateReservationStatus(reservationId, status.name)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Reservation not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}