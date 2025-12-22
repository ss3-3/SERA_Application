package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.event.UpdateAvailableSeatsUseCase
import com.example.sera_application.domain.usecase.reservation.CreateReservationUseCase
import com.example.sera_application.domain.usecase.reservation.UpdateReservationStatusUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservationFormState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val reservationId: String? = null,
    val totalPrice: Double = 0.0,
    val isFreeEvent: Boolean = false
)

@HiltViewModel
class ReservationFormViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val createReservationUseCase: CreateReservationUseCase,
    private val updateAvailableSeatsUseCase: UpdateAvailableSeatsUseCase,
    private val updateReservationStatusUseCase: UpdateReservationStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationFormState())
    val uiState: StateFlow<ReservationFormState> = _uiState.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val fetchedEvent = getEventByIdUseCase(eventId)
                _uiState.value = _uiState.value.copy(
                    event = fetchedEvent,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load event: ${e.message}"
                )
            }
        }
    }

    fun createReservation(
        eventId: String,
        quantities: Map<String, Int>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not logged in"
                )
                return@launch
            }

            val totalSeats = quantities.values.sum()
            if (totalSeats <= 0) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please select at least one ticket"
                )
                return@launch
            }

            val currentEvent = _uiState.value.event
            if (currentEvent == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Event data not loaded"
                )
                return@launch
            }

            // Validate seats availability before creating reservation
            val rockZoneRequested = quantities["Rock Zone"] ?: 0
            val normalZoneRequested = quantities["Normal Zone"] ?: 0

            if (rockZoneRequested > currentEvent.rockZoneSeats) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Not enough Rock Zone seats. Available: ${currentEvent.rockZoneSeats}, Requested: $rockZoneRequested"
                )
                return@launch
            }

            if (normalZoneRequested > currentEvent.normalZoneSeats) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Not enough Normal Zone seats. Available: ${currentEvent.normalZoneSeats}, Requested: $normalZoneRequested"
                )
                return@launch
            }

            var totalPrice = 0.0
            quantities.forEach { (zone, quantity) ->
                totalPrice += when (zone) {
                    "Rock Zone" -> currentEvent.rockZonePrice * quantity
                    "Normal Zone" -> currentEvent.normalZonePrice * quantity
                    else -> currentEvent.normalZonePrice * quantity
                }
            }

            // Check if event is free
            val isFreeEvent = totalPrice == 0.0
            // Set status to CONFIRMED for free events, PENDING for paid events
            val initialStatus = if (isFreeEvent) ReservationStatus.CONFIRMED else ReservationStatus.PENDING

            val reservation = EventReservation(
                reservationId = "",
                eventId = eventId,
                userId = userId,
                seats = totalSeats,
                rockZoneSeats = rockZoneRequested,
                normalZoneSeats = normalZoneRequested,
                totalPrice = totalPrice,
                status = initialStatus,
                createdAt = System.currentTimeMillis()
            )

            val result = createReservationUseCase(reservation)
            result.fold(
                onSuccess = { reservationId ->
                    // Update available seats (zone-specific)
                    val rockDelta = -(quantities["Rock Zone"] ?: 0)
                    val normalDelta = -(quantities["Normal Zone"] ?: 0)

                    updateAvailableSeatsUseCase(eventId, rockDelta, normalDelta)

                    // For free events, ensure status is CONFIRMED (in case it wasn't set correctly)
                    if (isFreeEvent) {
                        updateReservationStatusUseCase(reservationId, ReservationStatus.CONFIRMED.name)
                    }

                    // Reload event to get real-time updated seat counts
                    refreshEventData(eventId)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        error = null,
                        reservationId = reservationId,
                        totalPrice = totalPrice,
                        isFreeEvent = isFreeEvent
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = exception.message ?: "Failed to create reservation",
                        reservationId = null
                    )
                }
            )
        }
    }

    /**
     * Refresh event data to get real-time seat counts
     * Called after successful reservation to display updated availability
     */
    private fun refreshEventData(eventId: String) {
        viewModelScope.launch {
            try {
                val updatedEvent = getEventByIdUseCase(eventId)
                _uiState.value = _uiState.value.copy(event = updatedEvent)
            } catch (e: Exception) {
                // Silently fail - reservation was successful, just seat refresh failed
                println("Failed to refresh event data: ${e.message}")
            }
        }
    }

    fun clearSuccessState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, reservationId = null)
    }
}