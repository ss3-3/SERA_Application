package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.usecase.payment.GetPaymentHistoryUseCase
import com.example.sera_application.presentation.model.OrderData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val getPaymentHistoryUseCase: GetPaymentHistoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow("All Statuses")
    val statusFilter: StateFlow<String> = _statusFilter

    private val _dateFilter = MutableStateFlow("All Dates")
    val dateFilter: StateFlow<String> = _dateFilter

    private val _allOrders = MutableStateFlow<List<OrderData>>(emptyList())
    
    val filteredOrders: StateFlow<List<OrderData>> = combine(
        _allOrders,
        _searchQuery,
        _statusFilter,
        _dateFilter
    ) { orders, query, status, date ->
        orders.filter { order ->
            (query.isEmpty() || order.eventName.contains(query, ignoreCase = true) || order.orderId.contains(query, ignoreCase = true)) &&
            (status == "All Statuses" || order.status.equals(status, ignoreCase = true)) &&
            (date == "All Dates" || order.date == date)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPaymentHistory(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val payments = getPaymentHistoryUseCase(userId)
                
                // Convert Payment domain model to OrderData UI model
                val orders = payments.map { payment ->
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(payment.createdAt))
                    
                    OrderData(
                        eventName = payment.eventId, // Ideally fetch event name
                        orderId = payment.paymentId,
                        price = "RM ${String.format(Locale.US, "%.2f", payment.amount)}",
                        tickets = "1 ticket", // Ideally fetch quantity
                        status = payment.status.name, // Convert to friendly string if needed
                        date = dateStr
                    )
                }
                _allOrders.value = orders
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChanged(status: String) {
        _statusFilter.value = status
    }

    fun onDateFilterChanged(date: String) {
        _dateFilter.value = date
    }
}
