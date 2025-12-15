package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.ReservationFilter
import com.example.sera_application.domain.repository.ReservationRepository
import javax.inject.Inject

class FilterReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(filter: ReservationFilter): Result<List<EventReservation>> {
        return try {
            // Get base reservations
            val baseReservations = if (filter.userId != null) {
                reservationRepository.getUserReservations(filter.userId)
            } else if (filter.eventId != null) {
                reservationRepository.getEventReservations(filter.eventId)
            } else {
                reservationRepository.getUserReservations("") // Get all
            }
            
            // Apply filters
            val filtered = baseReservations.filter { reservation ->
                var matches = true
                
                // Filter by status
                if (filter.status != null && reservation.status != filter.status) {
                    matches = false
                }
                
                // Filter by date range
                if (filter.fromDate != null && reservation.createdAt < filter.fromDate) {
                    matches = false
                }
                
                if (filter.toDate != null && reservation.createdAt > filter.toDate) {
                    matches = false
                }
                
                // Filter by price range
                if (filter.minPrice != null && reservation.totalPrice < filter.minPrice) {
                    matches = false
                }
                
                if (filter.maxPrice != null && reservation.totalPrice > filter.maxPrice) {
                    matches = false
                }
                
                matches
            }
            
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
