package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetReservationByIdUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentScreenViewModel @Inject constructor(
    private val getReservationByIdUseCase: GetReservationByIdUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _reservation = MutableStateFlow<EventReservation?>(null)
    val reservation: StateFlow<EventReservation?> = _reservation

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadReservationDetails(reservationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val res = getReservationByIdUseCase(reservationId)
                if (res != null) {
                    _reservation.value = res
                    val eventDetails = getEventByIdUseCase(res.eventId)
                    _event.value = eventDetails
                    
                    val userDetails = getUserProfileUseCase(res.userId)
                    _user.value = userDetails
                } else {
                    _error.value = "Reservation not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load reservation details"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
