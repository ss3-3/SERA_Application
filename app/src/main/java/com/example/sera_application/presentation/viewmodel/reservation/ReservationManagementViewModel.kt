package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.ReservationWithDetails
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetAllReservationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationManagementViewModel @Inject constructor(
    private val getAllReservationsUseCase: GetAllReservationsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<ReservationWithDetails>>(emptyList())
    val reservations: StateFlow<List<ReservationWithDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val reservationList = getAllReservationsUseCase()
                
                // Fetch event details for each reservation (could be optimized)
                val enrichedList = reservationList.map { reservation ->
                    val event = getEventByIdUseCase(reservation.eventId)
                    ReservationWithDetails(reservation, event)
                }
                
                _reservations.value = enrichedList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load reservations"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
