package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.ReservationWithDetails
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetUserReservationsUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentByReservationIdUseCase
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
class ReservationListViewModel @Inject constructor(
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getPaymentByReservationIdUseCase: GetPaymentByReservationIdUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<ReservationWithDetails>>(emptyList())
    val reservations: StateFlow<List<ReservationWithDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun observeUserReservations(userId: String) {
        if (userId.isBlank()) {
            _error.value = "User ID cannot be blank"
            return
        }

        _isLoading.value = true
        _error.value = null

        getUserReservationsUseCase(userId)
            .onEach { reservationList ->
                // Fetch event details for each reservation
                android.util.Log.d("ReservationListVM", "Processing ${reservationList.size} reservations")
                val enrichedList = mutableListOf<ReservationWithDetails>()
                for (reservation in reservationList) {
                    val event = getEventByIdUseCase(reservation.eventId)
                    val payment = getPaymentByReservationIdUseCase(reservation.reservationId)
                    val paymentId = payment?.paymentId
                    android.util.Log.d("ReservationListVM", "ResId: ${reservation.reservationId}, PaymentId: $paymentId")
                    enrichedList.add(ReservationWithDetails(reservation, event, paymentId))
                }
                
                _reservations.value = enrichedList
                _isLoading.value = false
            }
            .catch { exception ->
                _error.value = exception.message ?: "Failed to observe reservations"
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun fetchReservations(userId: String) {
        observeUserReservations(userId)
    }
}
