package com.example.sera_application.presentation.viewmodel.payment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.usecase.payment.ValidatePaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val validatePaymentUseCase: ValidatePaymentUseCase
) : ViewModel() {

    private val _selectedPaymentMethod = MutableStateFlow("")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _eventDetails = MutableStateFlow<EventDetails?>(null)
    val eventDetails: StateFlow<EventDetails?> = _eventDetails

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
        _validationError.value = null
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        _validationError.value = null
    }

    fun updateName(newName: String) {
        _name.value = newName
        _validationError.value = null
    }

    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            // TODO: Load event details from repository
            // For now, you can set mock data or fetch from event repository
        }
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    fun validatePaymentDetails(
        userId: String,
        eventId: String,
        amount: Double,
        reservationId: String? = null
    ): Boolean {
        // Basic field validation
        if (email.value.isBlank()) {
            _validationError.value = "Email is required"
            return false
        }

        if (!isValidEmail(email.value)) {
            _validationError.value = "Invalid email format"
            return false
        }

        if (name.value.isBlank()) {
            _validationError.value = "Name is required"
            return false
        }

        if (selectedPaymentMethod.value.isBlank()) {
            _validationError.value = "Please select a payment method"
            return false
        }

        // Create payment object for validation
        val payment = Payment(
            paymentId = UUID.randomUUID().toString(),
            userId = userId,
            eventId = eventId,
            reservationId = reservationId,
            amount = amount,
            status = PaymentStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        // Validate using use case
        val isValid = validatePaymentUseCase(payment)

        if (!isValid) {
            _validationError.value = "Invalid payment details"
        }

        return isValid
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearValidationError() {
        _validationError.value = null
    }
}

data class EventDetails(
    val name: String,
    val date: String,
    val time: String,
    val venue: String,
    val ticketCount: Int,
    val totalPrice: Double
)