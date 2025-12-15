package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
) : ViewModel() {

    private val _selectedPaymentMethod = MutableStateFlow("")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _eventDetails = MutableStateFlow<EventDetails?>(null)
    val eventDetails: StateFlow<EventDetails?> = _eventDetails

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
        }
    }

    fun validatePaymentDetails(): Boolean {
        return email.value.isNotBlank() &&
                name.value.isNotBlank() &&
                selectedPaymentMethod.value.isNotBlank()
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