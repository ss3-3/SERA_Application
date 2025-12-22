package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.ReservationWithDetails
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.event.GetEventsByOrganizerUseCase
import com.example.sera_application.domain.usecase.reservation.GetEventReservationsUseCase
import com.example.sera_application.domain.usecase.reservation.UpdateReservationStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

@HiltViewModel
class ReservationManagementViewModel @Inject constructor(
    private val getEventReservationsUseCase: GetEventReservationsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getEventsByOrganizerUseCase: GetEventsByOrganizerUseCase,
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

        viewModelScope.launch {
        getEventReservationsUseCase(eventId)
            .onEach { reservationList ->
                // Fetch event details for each reservation using coroutine scope
                    val enrichedList = reservationList.map { reservation ->
                        async {
                            try {
                                val event = getEventByIdUseCase(reservation.eventId)
                                ReservationWithDetails(reservation, event)
                            } catch (e: Exception) {
                                ReservationWithDetails(reservation, null)
                            }
                        }
                    }.awaitAll()
                    
                    _reservations.value = enrichedList
                    _isLoading.value = false
            }
            .catch { exception ->
                _error.value = exception.message ?: "Failed to observe event reservations"
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
        }
    }

    /**
     * Load all reservations for all events created by the organizer
     * This observes reservations in real-time using Flow
     */
    fun loadOrganizerReservations(organizerId: String) {
        viewModelScope.launch {
            if (organizerId.isBlank()) {
                _error.value = "Organizer ID cannot be blank"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            try {
                // 1. Get all events created by this organizer
                android.util.Log.d("ReservationManagementVM", "Fetching events for organizer: $organizerId")
                val organizerEvents = getEventsByOrganizerUseCase(organizerId)
                
                if (organizerEvents.isEmpty()) {
                    android.util.Log.d("ReservationManagementVM", "No events found for organizer")
                    _reservations.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                android.util.Log.d("ReservationManagementVM", "Found ${organizerEvents.size} events for organizer")

                // 2. Create a map to store reservations by event ID
                val reservationsByEvent = mutableMapOf<String, List<ReservationWithDetails>>()
                var completedEvents = 0
                
                // 3. For each event, observe its reservations
                organizerEvents.forEach { event ->
                    getEventReservationsUseCase(event.eventId)
                        .onEach { reservationList ->
                            android.util.Log.d("ReservationManagementVM", 
                                "Got ${reservationList.size} reservations for event: ${event.name} (${event.eventId})")
                            
                            // Enrich reservations with event details
                            val enrichedList = reservationList.map { reservation ->
                                ReservationWithDetails(reservation, event)
                            }
                            
                            synchronized(reservationsByEvent) {
                                // Update the map
                                reservationsByEvent[event.eventId] = enrichedList
                                
                                // Combine all reservations from all events
                                val allReservations = reservationsByEvent.values.flatten()
                                    .sortedByDescending { it.reservation.createdAt }
                                _reservations.value = allReservations
                            }
                            
                            // Check if this is the first time we've received data for this event
                            // or if we've already counted it. Using a Set would be better but let's keep it simple.
                        }
                        .catch { exception ->
                            android.util.Log.e("ReservationManagementVM", 
                                "Error loading reservations for event ${event.eventId}: ${exception.message}")
                        }
                        .launchIn(viewModelScope)
                }
                
                // Allow some time for initial loads to complete before hiding loading spinner
                kotlinx.coroutines.delay(1500)
                _isLoading.value = false
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load organizer reservations"
                _isLoading.value = false
                android.util.Log.e("ReservationManagementVM", "Error: ${e.message}", e)
            }
        }
    }

    fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null

            try {
                val success = updateReservationStatusUseCase(reservationId, status.name)
                if (success) {
                    _isUpdating.value = false
                    // Flow will automatically update the list
                } else {
                    _error.value = "Failed to update reservation status"
                    _isUpdating.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update reservation status"
                _isUpdating.value = false
            }
        }
    }
}
