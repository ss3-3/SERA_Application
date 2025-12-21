package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.CancelReservationUseCase
import com.example.sera_application.domain.usecase.reservation.GetReservationByIdUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
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
    val error: String? = null,
    val isCancelling: Boolean = false
)

@HiltViewModel
class ReservationDetailsViewModel @Inject constructor(
    private val getReservationByIdUseCase: GetReservationByIdUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationDetailState())
    val uiState: StateFlow<ReservationDetailState> = _uiState.asStateFlow()

    fun loadReservation(reservationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch reservation using use case
                val reservation = getReservationByIdUseCase(reservationId)
                
                if (reservation != null) {
                    val event = try {
                        getEventByIdUseCase(reservation.eventId)
                    } catch (e: Exception) {
                        null
                    }
                    
                    val participant = try {
                        getUserProfileUseCase(reservation.userId)
                    } catch (e: Exception) {
                        null
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        reservation = reservation,
                        event = event,
                        participant = participant,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Reservation not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load details"
                )
            }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true, error = null)
            
            val result = cancelReservationUseCase(reservationId)
            result.fold(
                onSuccess = {
                    // Reload reservation to get updated status
                    loadReservation(reservationId)
                    _uiState.value = _uiState.value.copy(isCancelling = false)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        error = exception.message ?: "Failed to cancel reservation"
                    )
                }
            )
        }
    }
}
