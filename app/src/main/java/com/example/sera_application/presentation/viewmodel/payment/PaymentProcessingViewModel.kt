package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.data.api.PayPalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentProcessingViewModel @Inject constructor(
    private val paypalRepository: PayPalRepository
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    private val _transactionDetails = MutableStateFlow<TransactionDetails?>(null)
    val transactionDetails: StateFlow<TransactionDetails?> = _transactionDetails

    fun initiatePayPalPayment(
        amount: String,
        currency: String,
        description: String
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing

            try {
                val authResult = paypalRepository.authenticate()
                if (authResult.isFailure) {
                    _paymentState.value = PaymentState.Failed(
                        "Authentication failed: ${authResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                val orderResult = paypalRepository.createOrder(amount, currency, description)
                if (orderResult.isSuccess) {
                    val order = orderResult.getOrNull()!!
                    val approveLink = order.links.find { it.rel == "approve" }

                    if (approveLink != null) {
                        _paymentState.value = PaymentState.AwaitingApproval(approveLink.href)
                    } else {
                        _paymentState.value = PaymentState.Failed("Could not get approval URL")
                    }
                } else {
                    _paymentState.value = PaymentState.Failed(
                        "Order creation failed: ${orderResult.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Failed(e.message ?: "Unknown error")
            }
        }
    }

    fun capturePayment(orderId: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing

            try {
                val captureResult = paypalRepository.captureOrder(orderId)

                if (captureResult.isSuccess) {
                    val capture = captureResult.getOrNull()!!
                    _transactionDetails.value = TransactionDetails(
                        transactionId = capture.id,
                        amount = 70.0, // Get from capture response
                        paymentMethod = "PayPal",
                        date = "Nov 8, 2025",
                        time = "7:00 PM"
                    )
                    _paymentState.value = PaymentState.Success
                } else {
                    _paymentState.value = PaymentState.Failed(
                        captureResult.exceptionOrNull()?.message ?: "Capture failed"
                    )
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Failed(e.message ?: "Unknown error")
            }
        }
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }
}

sealed class PaymentState {
    object Idle : PaymentState()
    object Processing : PaymentState()
    data class AwaitingApproval(val approvalUrl: String) : PaymentState()
    object Success : PaymentState()
    data class Failed(val reason: String) : PaymentState()
}

data class TransactionDetails(
    val transactionId: String,
    val amount: Double,
    val paymentMethod: String,
    val date: String,
    val time: String
)