package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.presentation.ui.payment.OrderData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
) : ViewModel() {

    private val _allOrders = MutableStateFlow<List<OrderData>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("All")

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedFilter: StateFlow<String> = _selectedFilter

    val filteredOrders: StateFlow<List<OrderData>> = combine(
        _allOrders,
        _searchQuery,
        _selectedFilter
    ) { orders, query, filter ->
        orders.filter { order ->
            val matchesSearch = order.eventName.contains(query, ignoreCase = true) ||
                    order.orderId.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "All" -> true
                "Pending" -> order.status == "Refund Pending"
                "Paid" -> order.status == "Paid"
                "Refunded" -> order.status == "Refunded"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _summary = MutableStateFlow(PaymentSummary(0, 0, 0, 0.0))
    val summary: StateFlow<PaymentSummary> = _summary

    init {
        loadPaymentHistory()
    }

    fun loadPaymentHistory() {
        viewModelScope.launch {
            val orders = listOf(
                OrderData("MUSIC FIESTA 6.0", "1234-1234-1234", "RM 70.00", "2 tickets", "Refund Pending", "Jan 12, 2025"),
                OrderData("GUITAR Festival", "8456-2654-5952", "RM 60.00", "1 ticket", "Paid", "Jan 10, 2025"),
                OrderData("VOICHESTRA", "1234-1308-7566", "RM 180.00", "3 tickets", "Refunded", "Feb 25, 2025")
            )
            _allOrders.value = orders

            val total = orders.size
            val successful = orders.count { it.status == "Paid" }
            val refunded = orders.count { it.status == "Refunded" }
            val totalSpent = orders
                .filter { it.status == "Paid" }
                .sumOf { it.price.replace("RM ", "").toDoubleOrNull() ?: 0.0 }

            _summary.value = PaymentSummary(total, successful, refunded, totalSpent)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: String) {
        _selectedFilter.value = filter
    }
}

data class PaymentSummary(
    val totalPayments: Int,
    val successful: Int,
    val refunded: Int,
    val totalSpent: Double
)