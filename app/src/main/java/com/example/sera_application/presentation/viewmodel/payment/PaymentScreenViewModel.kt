package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.User
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetReservationByIdUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentByIdUseCase
import com.example.sera_application.domain.model.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentScreenViewModel @Inject constructor(
    private val getReservationByIdUseCase: GetReservationByIdUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getPaymentByIdUseCase: GetPaymentByIdUseCase
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

    fun loadPaymentDetails(paymentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val payment = getPaymentByIdUseCase(paymentId)
                if (payment != null) {
                    // Load event details
                    val eventDetails = getEventByIdUseCase(payment.eventId)
                    _event.value = eventDetails

                    // Load user details
                    val userDetails = getUserProfileUseCase(payment.userId)
                    _user.value = userDetails

                    // Load reservation if available
                    payment.reservationId?.let { resId ->
                        val res = getReservationByIdUseCase(resId)
                        _reservation.value = res
                    } ?: run {
                        // If no reservation, create a minimal reservation from payment data
                        _reservation.value = EventReservation(
                            reservationId = "",
                            eventId = payment.eventId,
                            userId = payment.userId,
                            seats = 0, // Will need to be calculated or stored
                            totalPrice = payment.amount,
                            status = com.example.sera_application.domain.model.enums.ReservationStatus.PENDING
                        )
                    }
                } else {
                    _error.value = "Payment not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load payment details"
            } finally {
                _isLoading.value = false
            }
        }
    }
}