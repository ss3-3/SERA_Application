package com.example.sera_application.presentation.viewmodel.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.ReservationWithDetails
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.reservation.GetUserReservationsUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentByReservationIdUseCase
import com.example.sera_application.domain.usecase.reservation.CancelReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ReservationListViewModel @Inject constructor(
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val getPaymentByReservationIdUseCase: GetPaymentByReservationIdUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase
) : ViewModel() {

    private val _reservations = MutableStateFlow<List<ReservationWithDetails>>(emptyList())
    val reservations: StateFlow<List<ReservationWithDetails>> = _reservations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling: StateFlow<Boolean> = _isCancelling.asStateFlow()

    private val _cancelSuccess = MutableStateFlow<String?>(null)
    val cancelSuccess: StateFlow<String?> = _cancelSuccess.asStateFlow()

    fun observeUserReservations(userId: String) {
        if (userId.isBlank()) {
            _error.value = "User ID cannot be blank"
            return
        }

        _isLoading.value = true
        _error.value = null

        // Call suspend function within coroutine scope and collect the Flow
        viewModelScope.launch {
            try {
                getUserReservationsUseCase(userId)
                    .onEach { reservationList ->
                        // Fetch event details for each reservation
                        android.util.Log.d("ReservationListVM", "Processing ${reservationList.size} reservations")
                        
                        // Use async to parallelize suspend function calls
                        val enrichedList = reservationList.map { reservation ->
                            async {
                                val event = getEventByIdUseCase(reservation.eventId)
                                val payment = getPaymentByReservationIdUseCase(reservation.reservationId)
                                val paymentId = payment?.paymentId
                                android.util.Log.d("ReservationListVM", "ResId: ${reservation.reservationId}, PaymentId: $paymentId")
                                ReservationWithDetails(reservation, event, paymentId)
                            }
                        }.awaitAll()
                        
                        _reservations.value = enrichedList
                        _isLoading.value = false
                    }
                    .catch { exception ->
                        _error.value = exception.message ?: "Failed to observe reservations"
                        _isLoading.value = false
                    }
                    .collect() // Collect the Flow
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load reservations"
                _isLoading.value = false
            }
        }
    }

    fun fetchReservations(userId: String) {
        observeUserReservations(userId)
    }

    fun cancelReservation(reservationId: String, userId: String) {
        viewModelScope.launch {
            _isCancelling.value = true
            _error.value = null
            _cancelSuccess.value = null

            try {
                val result = cancelReservationUseCase(reservationId)
                result.fold(
                    onSuccess = {
                        _cancelSuccess.value = "Reservation cancelled successfully. Refund will be processed within 3-5 business days."
                        _isCancelling.value = false
                        // Refresh the reservations list
                        fetchReservations(userId)
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Failed to cancel reservation"
                        _isCancelling.value = false
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
                _isCancelling.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _cancelSuccess.value = null
    }
}
