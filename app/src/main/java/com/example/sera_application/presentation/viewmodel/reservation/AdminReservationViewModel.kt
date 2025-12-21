package com.example.sera_application.presentation.viewmodel.reservation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.reservation.GetAllReservationsUseCase
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservationWithFullDetails(
    val reservation: EventReservation,
    val event: Event? = null,
    val user: User? = null
)

@HiltViewModel
class AdminReservationViewModel @Inject constructor(
    private val getAllReservationsUseCase: GetAllReservationsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<ReservationWithFullDetails>>(emptyList())
    val reservations: StateFlow<List<ReservationWithFullDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadAllReservations() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                Log.d("AdminReservationVM", "Loading all reservations...")
                
                val reservationList = getAllReservationsUseCase()
                Log.d("AdminReservationVM", "Received ${reservationList.size} reservations")
                
                // Fetch event and user details for each reservation
                val reservationsWithDetails = reservationList.map { reservation ->
                    val event = try {
                        getEventByIdUseCase(reservation.eventId)
                    } catch (e: Exception) {
                        Log.e("AdminReservationVM", "Error fetching event ${reservation.eventId}: ${e.message}")
                        null
                    }
                    
                    val user = try {
                        getUserProfileUseCase(reservation.userId)
                    } catch (e: Exception) {
                        Log.e("AdminReservationVM", "Error fetching user ${reservation.userId}: ${e.message}")
                        null
                    }
                    
                    ReservationWithFullDetails(
                        reservation = reservation,
                        event = event,
                        user = user
                    )
                }
                
                _reservations.value = reservationsWithDetails
                _isLoading.value = false
                    
            } catch (e: Exception) {
                Log.e("AdminReservationVM", "Error in loadAllReservations: ${e.message}", e)
                _error.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }
}

