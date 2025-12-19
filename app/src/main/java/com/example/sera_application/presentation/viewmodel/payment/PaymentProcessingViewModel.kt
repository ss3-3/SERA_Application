package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.data.remote.paypal.repository.PayPalRepository
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.usecase.payment.ProcessPaymentUseCase
import com.example.sera_application.domain.usecase.payment.ValidatePaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentProcessingViewModel @Inject constructor(
    private val paypalRepository: PayPalRepository,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val validatePaymentUseCase: ValidatePaymentUseCase
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    private val _transactionDetails = MutableStateFlow<TransactionDetails?>(null)
    val transactionDetails: StateFlow<TransactionDetails?> = _transactionDetails

    fun initiatePayPalPayment(
        amount: String,
        currency: String,
        description: String,
        userId: String,
        eventId: String,
        reservationId: String? = null
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing

            try {
                val amountDouble = amount.toDoubleOrNull()
                if (amountDouble == null || amountDouble <= 0) {
                    _paymentState.value = PaymentState.Failed("Invalid amount")
                    return@launch
                }

                // Create temporary payment object for validation
                val payment = Payment(
                    paymentId = UUID.randomUUID().toString(),
                    userId = userId,
                    eventId = eventId,
                    reservationId = reservationId,
                    amount = amountDouble,
                    status = PaymentStatus.PENDING,
                    createdAt = System.currentTimeMillis()
                )

                // Validate payment using use case
                if (!validatePaymentUseCase(payment)) {
                    _paymentState.value = PaymentState.Failed("Invalid payment details")
                    return@launch
                }

                // Authenticate with PayPal
                val authResult = paypalRepository.authenticate()
                if (authResult.isFailure) {
                    _paymentState.value = PaymentState.Failed(
                        "Authentication failed: ${authResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }

                // Create PayPal order
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

    fun capturePayment(
        orderId: String,
        userId: String,
        eventId: String,
        amount: Double,
        reservationId: String? = null
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing

            try {
                val captureResult = paypalRepository.captureOrder(orderId)

                if (captureResult.isSuccess) {
                    val capture = captureResult.getOrNull()!!

                    // Create payment object
                    val payment = Payment(
                        paymentId = capture.id,
                        userId = userId,
                        eventId = eventId,
                        reservationId = reservationId,
                        amount = amount,
                        status = PaymentStatus.SUCCESS,
                        createdAt = System.currentTimeMillis()
                    )

                    // Process payment using use case
                    val success = processPaymentUseCase(payment)

                    if (success) {
                        _transactionDetails.value = TransactionDetails(
                            transactionId = capture.id,
                            amount = payment.amount,
                            paymentMethod = "PayPal",
                            date = "Nov 8, 2025",
                            time = "7:00 PM"
                        )
                        _paymentState.value = PaymentState.Success
                    } else {
                        _paymentState.value = PaymentState.Failed("Payment processing failed")
                    }
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