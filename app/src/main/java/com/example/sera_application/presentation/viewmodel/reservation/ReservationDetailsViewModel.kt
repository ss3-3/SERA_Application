package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.repository.EventRepository
import com.example.sera_application.domain.repository.ReservationRepository
import com.example.sera_application.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservationDetailState(
    val reservation: EventReservation? = null,
    val event: Event? = null,
    val participant: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReservationDetailsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationDetailState())
    val uiState: StateFlow<ReservationDetailState> = _uiState.asStateFlow()

    fun loadReservation(reservationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch reservation
                val reservation = reservationRepository.getReservationById(reservationId)
                
                if (reservation != null) {
                    val event = eventRepository.getEventById(reservation.eventId)
                    val participant = userRepository.getUserById(reservation.userId)
                    
                    _uiState.value = _uiState.value.copy(
                        reservation = reservation,
                        event = event,
                        participant = participant,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Reservation not found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Failed to load details")
            }
        }
    }
}
