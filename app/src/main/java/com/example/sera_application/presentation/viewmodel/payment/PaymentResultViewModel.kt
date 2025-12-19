package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.usecase.payment.GetPaymentByIdUseCase
import com.example.sera_application.domain.usecase.payment.RefundPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PaymentResultViewModel @Inject constructor(
    private val refundPaymentUseCase: RefundPaymentUseCase,
    private val getPaymentByIdUseCase: GetPaymentByIdUseCase
) : ViewModel() {

    private val _refundState = MutableStateFlow<RefundState>(RefundState.Idle)
    val refundState: StateFlow<RefundState> = _refundState

    private val _selectedRefundReason = MutableStateFlow("")
    val selectedRefundReason: StateFlow<String> = _selectedRefundReason

    private val _additionalNotes = MutableStateFlow("")
    val additionalNotes: StateFlow<String> = _additionalNotes

    private val _receiptFile = MutableStateFlow<File?>(null)
    val receiptFile: StateFlow<File?> = _receiptFile

    private val _paymentDetails = MutableStateFlow<ReceiptData?>(null)
    val paymentDetails: StateFlow<ReceiptData?> = _paymentDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun selectRefundReason(reason: String) {
        _selectedRefundReason.value = reason
    }

    fun updateAdditionalNotes(notes: String) {
        _additionalNotes.value = notes
    }

    fun loadPaymentDetails(paymentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val payment = getPaymentByIdUseCase(paymentId)

                if (payment != null) {
                    // Format timestamp
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val date = Date(payment.createdAt)

                    // Convert to receipt data
                    _paymentDetails.value = ReceiptData(
                        eventName = payment.eventId, // You may need to fetch event name
                        transactionId = payment.paymentId,
                        date = dateFormat.format(date),
                        time = timeFormat.format(date),
                        venue = "Unknown", // Fetch from event details if available
                        ticketType = "General",
                        quantity = 1, // You may need to get from reservation
                        seats = "N/A",
                        price = payment.amount,
                        email = "",
                        name = "",
                        phone = ""
                    )
                }
            } catch (e: Exception) {
                // Handle error silently or set error state
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitRefundRequest(
        paymentId: String,
        reason: String,
        notes: String
    ) {
        viewModelScope.launch {
            _refundState.value = RefundState.Processing

            try {
                // Validate inputs
                if (paymentId.isBlank()) {
                    _refundState.value = RefundState.Failed("Invalid payment ID")
                    return@launch
                }

                if (reason.isBlank()) {
                    _refundState.value = RefundState.Failed("Please select a refund reason")
                    return@launch
                }

                // Verify payment exists and is eligible for refund
                val payment = getPaymentByIdUseCase(paymentId)
                if (payment == null) {
                    _refundState.value = RefundState.Failed("Payment not found")
                    return@launch
                }

                if (payment.status != PaymentStatus.SUCCESS) {
                    _refundState.value = RefundState.Failed("Payment is not eligible for refund")
                    return@launch
                }

                // Process refund using use case
                val success = refundPaymentUseCase(paymentId)

                if (success) {
                    _refundState.value = RefundState.Success("Refund request submitted successfully")
                } else {
                    _refundState.value = RefundState.Failed("Failed to process refund request")
                }
            } catch (e: Exception) {
                _refundState.value = RefundState.Failed(e.message ?: "Failed to submit refund")
            }
        }
    }

    fun generateReceipt(receiptData: ReceiptData): File? {
        // TODO: Implement receipt generation
        // You can use PDF library to generate receipt
        return null
    }

    fun resetRefundState() {
        _refundState.value = RefundState.Idle
        _selectedRefundReason.value = ""
        _additionalNotes.value = ""
    }
}

sealed class RefundState {
    object Idle : RefundState()
    object Processing : RefundState()
    data class Success(val message: String) : RefundState()
    data class Failed(val error: String) : RefundState()
}

data class ReceiptData(
    val eventName: String,
    val transactionId: String,
    val date: String,
    val time: String,
    val venue: String,
    val ticketType: String,
    val quantity: Int,
    val seats: String,
    val price: Double,
    val email: String,
    val name: String,
    val phone: String
)