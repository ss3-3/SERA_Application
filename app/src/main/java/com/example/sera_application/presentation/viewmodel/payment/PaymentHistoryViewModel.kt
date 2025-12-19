package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.enums.PaymentStatus
import com.example.sera_application.domain.usecase.payment.GetPaymentHistoryUseCase
import com.example.sera_application.presentation.ui.payment.OrderData
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

    private val _allOrders = MutableStateFlow<List<OrderData>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("All")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedFilter: StateFlow<String> = _selectedFilter
    val isLoading: StateFlow<Boolean> = _isLoading
    val error: StateFlow<String?> = _error

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

    fun loadPaymentHistory(userId: String = "current_user_id") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get payment history from use case
                val payments = getPaymentHistoryUseCase(userId)

                // Convert Payment domain model to OrderData UI model
                val orders = payments.map { payment ->
                    OrderData(
                        eventName = payment.eventId, // You may need to fetch event name separately
                        orderId = payment.paymentId,
                        price = "RM ${String.format("%.2f", payment.amount)}",
                        tickets = "1 ticket", // You may need to get this from reservation
                        status = when (payment.status) {
                            PaymentStatus.SUCCESS -> "Paid"
                            PaymentStatus.REFUNDED -> "Refunded"
                            PaymentStatus.REFUND_PENDING -> "Refund Pending"
                            PaymentStatus.PENDING -> "Pending"
                            PaymentStatus.PROCESSING -> "Processing"
                            PaymentStatus.FAILED -> "Failed"
                        },
                        date = formatDate(payment.createdAt)
                    )
                }

                _allOrders.value = orders

                // Calculate summary
                val total = orders.size
                val successful = payments.count { it.status == PaymentStatus.SUCCESS }
                val refunded = payments.count { it.status == PaymentStatus.REFUNDED }
                val totalSpent = payments
                    .filter { it.status == PaymentStatus.SUCCESS }
                    .sumOf { it.amount }

                _summary.value = PaymentSummary(total, successful, refunded, totalSpent)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load payment history"

                // Fallback to mock data for demo
                loadMockData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun loadMockData() {
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