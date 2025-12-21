package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetReservationByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentScreenViewModel @Inject constructor(
    private val getReservationByIdUseCase: GetReservationByIdUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase
) : ViewModel() {

    private val _reservation = MutableStateFlow<EventReservation?>(null)
    val reservation: StateFlow<EventReservation?> = _reservation.asStateFlow()

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadReservationDetails(reservationId: String) {
        if (reservationId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val reservation = getReservationByIdUseCase(reservationId)
                _reservation.value = reservation

                // Load event details if reservation exists
                if (reservation != null) {
                    val event = getEventByIdUseCase(reservation.eventId)
                    _event.value = event
                }
            } catch (e: Exception) {
                // Handle error silently or set error state if needed
            } finally {
                _isLoading.value = false
            }
        }
    }
}

