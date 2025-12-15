package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PaymentResultViewModel @Inject constructor(
) : ViewModel() {

    private val _refundState = MutableStateFlow<RefundState>(RefundState.Idle)
    val refundState: StateFlow<RefundState> = _refundState

    private val _selectedRefundReason = MutableStateFlow("")
    val selectedRefundReason: StateFlow<String> = _selectedRefundReason

    private val _additionalNotes = MutableStateFlow("")
    val additionalNotes: StateFlow<String> = _additionalNotes

    private val _receiptFile = MutableStateFlow<File?>(null)
    val receiptFile: StateFlow<File?> = _receiptFile

    fun selectRefundReason(reason: String) {
        _selectedRefundReason.value = reason
    }

    fun updateAdditionalNotes(notes: String) {
        _additionalNotes.value = notes
    }

    fun submitRefundRequest(
        orderId: String,
        reason: String,
        notes: String
    ) {
        viewModelScope.launch {
            _refundState.value = RefundState.Processing

            try {
                kotlinx.coroutines.delay(1500)

                _refundState.value = RefundState.Success("Refund request submitted successfully")
            } catch (e: Exception) {
                _refundState.value = RefundState.Failed(e.message ?: "Failed to submit refund")
            }
        }
    }

    fun generateReceipt(receiptData: ReceiptData): File? {
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