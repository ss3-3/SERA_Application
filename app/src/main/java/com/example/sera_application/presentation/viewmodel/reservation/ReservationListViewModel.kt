package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.usecase.reservation.GetUserReservationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationListViewModel @Inject constructor(
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val getEventByIdUseCase: com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<com.example.sera_application.domain.model.ReservationWithDetails>>(emptyList())
    val reservations: StateFlow<List<com.example.sera_application.domain.model.ReservationWithDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchReservations(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val reservationList = getUserReservationsUseCase(userId)
                
                // Fetch event details for each reservation
                val enrichedList = reservationList.map { reservation ->
                    val event = getEventByIdUseCase(reservation.eventId)
                    com.example.sera_application.domain.model.ReservationWithDetails(reservation, event)
                }
                
                _reservations.value = enrichedList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to fetch reservations"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
