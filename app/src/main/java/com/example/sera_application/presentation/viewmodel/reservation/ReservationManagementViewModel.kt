package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.ReservationWithDetails
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetEventReservationsUseCase
import com.example.sera_application.domain.usecase.reservation.UpdateReservationStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationManagementViewModel @Inject constructor(
    private val getEventReservationsUseCase: GetEventReservationsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateReservationStatusUseCase: UpdateReservationStatusUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<ReservationWithDetails>>(emptyList())
    val reservations: StateFlow<List<ReservationWithDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    fun observeEventReservations(eventId: String) {
        if (eventId.isBlank()) {
            _error.value = "Event ID cannot be blank"
            return
        }

        _isLoading.value = true
        _error.value = null

        getEventReservationsUseCase(eventId)
            .onEach { reservationList ->
                // Fetch event details for each reservation
                val enrichedList = reservationList.map { reservation ->
                    try {
                        val event = getEventByIdUseCase(reservation.eventId)
                        ReservationWithDetails(reservation, event)
                    } catch (e: Exception) {
                        ReservationWithDetails(reservation, null)
                    }
                }
                
                _reservations.value = enrichedList
                _isLoading.value = false
            }
            .catch { exception ->
                _error.value = exception.message ?: "Failed to observe event reservations"
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null

            val result = updateReservationStatusUseCase(reservationId, status)
            result.fold(
                onSuccess = {
                    _isUpdating.value = false
                    // Flow will automatically update the list
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Failed to update reservation status"
                    _isUpdating.value = false
                }
            )
        }
    }
}
