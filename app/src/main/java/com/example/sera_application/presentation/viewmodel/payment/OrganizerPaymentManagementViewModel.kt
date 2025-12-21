package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.ReservationWithDetails
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.usecase.event.GetEventsByOrganizerUseCase
import com.example.sera_application.domain.usecase.reservation.GetEventReservationsUseCase
import com.example.sera_application.domain.usecase.reservation.UpdateReservationStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrganizerPaymentManagementViewModel @Inject constructor(
    private val getEventsByOrganizerUseCase: GetEventsByOrganizerUseCase,
    private val getEventReservationsUseCase: GetEventReservationsUseCase,
    private val updateReservationStatusUseCase: UpdateReservationStatusUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<ReservationWithDetails>>(emptyList())
    val reservations: StateFlow<List<ReservationWithDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _events = MutableStateFlow<List<com.example.sera_application.domain.model.Event>>(emptyList())
    val events: StateFlow<List<com.example.sera_application.domain.model.Event>> = _events.asStateFlow()

    fun loadOrganizerData(organizerId: String) {
        viewModelScope.launch {
            if (organizerId.isBlank()) return@launch

            _isLoading.value = true
            _error.value = null

            try {
                val organizerEvents = getEventsByOrganizerUseCase(organizerId)
                _events.value = organizerEvents

                if (organizerEvents.isEmpty()) {
                    _reservations.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val reservationsByEvent = mutableMapOf<String, List<ReservationWithDetails>>()
                
                organizerEvents.forEach { event ->
                    getEventReservationsUseCase(event.eventId)
                        .onEach { reservationList ->
                            val enrichedList = reservationList.map { reservation ->
                                ReservationWithDetails(reservation, event)
                            }
                            
                            synchronized(reservationsByEvent) {
                                reservationsByEvent[event.eventId] = enrichedList
                                val allReservations = reservationsByEvent.values.flatten()
                                    .sortedByDescending { it.reservation.createdAt }
                                _reservations.value = allReservations
                            }
                        }
                        .catch { e ->
                            android.util.Log.e("OrganizerPaymentVM", "Error for event ${event.eventId}: ${e.message}")
                        }
                        .launchIn(viewModelScope)
                }

                kotlinx.coroutines.delay(1500)
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load data"
                _isLoading.value = false
            }
        }
    }

    fun updateRefundStatus(reservationId: String, approve: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // If special refund logic is needed, use a separate use case.
                // For now, we reuse updateReservationStatus.
                val newStatus = if (approve) ReservationStatus.CANCELLED else ReservationStatus.CONFIRMED
                updateReservationStatusUseCase(reservationId, newStatus.name)
                // Flow updates will automatically refresh the list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
