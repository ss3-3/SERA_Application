package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentByIdUseCase
import com.example.sera_application.domain.usecase.payment.RefundPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefundRequestViewModel @Inject constructor(
    private val getPaymentByIdUseCase: GetPaymentByIdUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val refundPaymentUseCase: RefundPaymentUseCase,
    private val updateReservationStatusUseCase: com.example.sera_application.domain.usecase.reservation.UpdateReservationStatusUseCase
) : ViewModel() {

    private val _payment = MutableStateFlow<Payment?>(null)
    val payment: StateFlow<Payment?> = _payment.asStateFlow()

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _refundSuccess = MutableStateFlow(false)
    val refundSuccess: StateFlow<Boolean> = _refundSuccess.asStateFlow()

    fun loadPaymentData(paymentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val paymentData = getPaymentByIdUseCase(paymentId)
                if (paymentData != null) {
                    _payment.value = paymentData
                    // Load event details
                    val eventData = getEventByIdUseCase(paymentData.eventId)
                    _event.value = eventData
                } else {
                    _error.value = "Payment not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load payment data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitRefundRequest(reason: String, notes: String) {
        viewModelScope.launch {
            val paymentId = _payment.value?.paymentId ?: return@launch
            _isLoading.value = true
            _error.value = null
            try {
                val success = refundPaymentUseCase(paymentId)
                if (success) {
                    // Update reservation status to CANCELLED
                    _payment.value?.reservationId?.let { resId ->
                        updateReservationStatusUseCase(resId, "CANCELLED")
                    }
                    _refundSuccess.value = true
                } else {
                    _error.value = "Failed to submit refund request"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to submit refund request"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

