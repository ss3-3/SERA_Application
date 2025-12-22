package com.example.sera_application.presentation.viewmodel.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.usecase.event.GetEventsByOrganizerUseCase
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentsByEventUseCase
import com.example.sera_application.domain.usecase.payment.GetPaymentHistoryUseCase
import com.example.sera_application.domain.usecase.payment.ApproveRefundUseCase
import com.example.sera_application.domain.usecase.reservation.GetEventReservationsUseCase
import com.example.sera_application.domain.usecase.user.GetUserProfileUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sera_application.data.mapper.PaymentFirestoreMapper.toPayment
import com.example.sera_application.presentation.ui.payment.PaymentInfo
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrganizerPaymentManagementViewModel @Inject constructor(
    private val getEventsByOrganizerUseCase: GetEventsByOrganizerUseCase,
    private val getPaymentsByEventUseCase: GetPaymentsByEventUseCase,
    private val getEventReservationsUseCase: GetEventReservationsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val approveRefundUseCase: ApproveRefundUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _events = MutableStateFlow<List<com.example.sera_application.domain.model.Event>>(emptyList())
    val events: StateFlow<List<com.example.sera_application.domain.model.Event>> = _events.asStateFlow()

    private val _paymentInfoList = MutableStateFlow<List<PaymentInfo>>(emptyList())
    val paymentInfoList: StateFlow<List<PaymentInfo>> = _paymentInfoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadOrganizerEvents() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                android.util.Log.e("OrganizerPaymentVM", "User not logged in")
                _error.value = "User not logged in"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            try {
                android.util.Log.d("OrganizerPaymentVM", "Loading events for organizer: ${currentUser.uid}")
                val organizerEvents = getEventsByOrganizerUseCase(currentUser.uid)
                android.util.Log.d("OrganizerPaymentVM", "Loaded ${organizerEvents.size} events for organizer")
                organizerEvents.forEach { event ->
                    android.util.Log.d("OrganizerPaymentVM", "Event: ${event.name} (ID: ${event.eventId})")
                }
                _events.value = organizerEvents
            } catch (e: Exception) {
                android.util.Log.e("OrganizerPaymentVM", "Error loading events: ${e.message}", e)
                _error.value = "Failed to load events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPaymentsForEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                android.util.Log.d("OrganizerPaymentVM", "Starting to load payments for eventId: $eventId")
                
                // Verify eventId is not empty
                if (eventId.isBlank()) {
                    android.util.Log.e("OrganizerPaymentVM", "EventId is blank!")
                    _error.value = "Invalid event ID"
                    _paymentInfoList.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                // Get current user to verify they're an organizer
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    android.util.Log.e("OrganizerPaymentVM", "User not logged in")
                    _error.value = "User not logged in"
                    _paymentInfoList.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                android.util.Log.d("OrganizerPaymentVM", "Current user ID: ${currentUser.uid}")
                
                // CRITICAL: Verify event ownership directly from Firestore BEFORE querying payments
                // This ensures we have the most up-to-date event data and can verify ownership
                // before Firestore security rules validate the payment query
                android.util.Log.d("OrganizerPaymentVM", "Verifying event ownership for eventId: $eventId")
                val event = getEventByIdUseCase(eventId)
                
                if (event == null) {
                    android.util.Log.e("OrganizerPaymentVM", "Event $eventId not found in Firestore")
                    _error.value = "Event not found. Please ensure the event exists."
                    _paymentInfoList.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                // Verify the event's organizerId matches the current user
                if (event.organizerId != currentUser.uid) {
                    android.util.Log.e("OrganizerPaymentVM", "Event $eventId belongs to organizer ${event.organizerId}, but current user is ${currentUser.uid}")
                    _error.value = "You don't have permission to view payments for this event"
                    _paymentInfoList.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                android.util.Log.d("OrganizerPaymentVM", "✅ Verified event $eventId belongs to organizer ${currentUser.uid}")
                android.util.Log.d("OrganizerPaymentVM", "Event name: ${event.name}, organizerId: ${event.organizerId}")
                
                // Try getting payments directly by eventId first - this queries Firebase
                android.util.Log.d("OrganizerPaymentVM", "=== CALLING getPaymentsByEventUseCase with eventId: '$eventId' ===")
                var payments = getPaymentsByEventUseCase(eventId)
                android.util.Log.d("OrganizerPaymentVM", "✅ Loaded ${payments.size} payments from Firebase for eventId: '$eventId'")
                
                // If no payments found, check if it's a permission issue
                if (payments.isEmpty()) {
                    android.util.Log.w("OrganizerPaymentVM", "⚠️ No payments found. Checking if this is a permission issue...")
                    android.util.Log.w("OrganizerPaymentVM", "   - EventId: $eventId")
                    android.util.Log.w("OrganizerPaymentVM", "   - UserId: ${currentUser.uid}")
                    android.util.Log.w("OrganizerPaymentVM", "   - This might be a Firestore security rules issue")
                    android.util.Log.w("OrganizerPaymentVM", "   - Verify: 1) User role is 'ORGANIZER' in users collection")
                    android.util.Log.w("OrganizerPaymentVM", "   - Verify: 2) Event exists and organizerId matches ${currentUser.uid}")
                    android.util.Log.w("OrganizerPaymentVM", "   - Verify: 3) Payment documents have correct eventId field")
                }
                
                // Verify all payments have the correct eventId
                payments.forEachIndexed { index, payment ->
                    android.util.Log.d("OrganizerPaymentVM", "Payment[$index] from Firebase: paymentId=${payment.paymentId}, eventId='${payment.eventId}', status=${payment.status.name}")
                    if (payment.eventId != eventId) {
                        android.util.Log.e("OrganizerPaymentVM", "⚠️ MISMATCH: Payment ${payment.paymentId} has eventId='${payment.eventId}' but query was for eventId='$eventId'")
                    }
                }
                
                // If no payments found, log a warning but don't try to query all payments
                // Querying all payments might cause permission denied errors for organizers
                if (payments.isEmpty()) {
                    android.util.Log.w("OrganizerPaymentVM", "No payments found with direct query for eventId: $eventId")
                    android.util.Log.w("OrganizerPaymentVM", "This could mean:")
                    android.util.Log.w("OrganizerPaymentVM", "1. No payments exist for this event")
                    android.util.Log.w("OrganizerPaymentVM", "2. Payments exist but eventId field doesn't match")
                    android.util.Log.w("OrganizerPaymentVM", "3. Firestore security rules may be blocking the query")
                }
                
                // Log all payment eventIds for debugging
                payments.forEach { payment ->
                    android.util.Log.d("OrganizerPaymentVM", "Payment eventId: ${payment.eventId}, matches query: ${payment.eventId == eventId}")
                }
                
                if (payments.isEmpty()) {
                    android.util.Log.w("OrganizerPaymentVM", "No payments found for eventId: $eventId")
                    android.util.Log.w("OrganizerPaymentVM", "This could mean:")
                    android.util.Log.w("OrganizerPaymentVM", "1. No payments exist for this event")
                    android.util.Log.w("OrganizerPaymentVM", "2. Payments exist but eventId field doesn't match")
                    android.util.Log.w("OrganizerPaymentVM", "3. Firestore security rules may be blocking the query")
                    // Continue processing to show empty list in UI
                } else {
                    // Log payment details for debugging
                payments.forEachIndexed { index, payment ->
                    android.util.Log.d("OrganizerPaymentVM", "Payment[$index]: id=${payment.paymentId}, eventId=${payment.eventId}, userId=${payment.userId}, amount=${payment.amount}, status=${payment.status.name}")
                    
                    // Log specifically for REFUND_PENDING payments
                    if (payment.status.name == "REFUND_PENDING") {
                        android.util.Log.d("OrganizerPaymentVM", "*** REFUND_PENDING payment detected: ${payment.paymentId}, will be converted to PaymentInfo")
                    }
                }
                }
                
                // Get reservations for this event to map reservation data
                android.util.Log.d("OrganizerPaymentVM", "Loading reservations for eventId: $eventId")
                val reservations = try {
                    // Collect the first emission from the Flow safely
                    // Using a mutable list to capture the first value
                    var reservationList: List<com.example.sera_application.domain.model.EventReservation> = emptyList()
                    getEventReservationsUseCase(eventId)
                        .take(1)
                        .collect { list ->
                            reservationList = list
                        }
                    reservationList
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // If flow was cancelled/aborted, return empty list
                    // This catches AbortFlowException (which extends CancellationException)
                    android.util.Log.w("OrganizerPaymentVM", "Flow was cancelled/aborted, using empty reservations list")
                    emptyList()
                } catch (e: Exception) {
                    android.util.Log.e("OrganizerPaymentVM", "Error loading reservations: ${e.message}", e)
                    emptyList()
                }
                android.util.Log.d("OrganizerPaymentVM", "Loaded ${reservations.size} reservations for eventId: $eventId")
                val reservationMap = reservations.associateBy { it.reservationId }

                // Convert to PaymentInfo
                android.util.Log.d("OrganizerPaymentVM", "Converting ${payments.size} payments to PaymentInfo")
                val paymentInfoList = convertPaymentsToPaymentInfo(payments, eventId, reservationMap)
                android.util.Log.d("OrganizerPaymentVM", "Converted to ${paymentInfoList.size} PaymentInfo objects")
                _paymentInfoList.value = paymentInfoList
                
                // Clear any previous errors if we successfully loaded payments (even if empty)
                if (paymentInfoList.isNotEmpty() || payments.isEmpty()) {
                    _error.value = null
                }
            } catch (e: Exception) {
                android.util.Log.e("OrganizerPaymentVM", "Error loading payments: ${e.message}", e)
                e.printStackTrace()
                
                // Check if it's a permission denied error
                val errorMessage = when {
                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                        "Permission denied. Please check Firestore security rules for organizer access to payments."
                    }
                    e.message?.contains("permission", ignoreCase = true) == true -> {
                        "Permission error: ${e.message}. Please ensure your organizer account has proper permissions."
                    }
                    else -> "Failed to load payments: ${e.message}"
                }
                
                _error.value = errorMessage
                _paymentInfoList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun convertPaymentsToPaymentInfo(
        payments: List<Payment>,
        eventId: String,
        reservationMap: Map<String, com.example.sera_application.domain.model.EventReservation>
    ): List<PaymentInfo> {
        val paymentInfoList = mutableListOf<PaymentInfo>()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        android.util.Log.d("OrganizerPaymentVM", "Converting ${payments.size} payments to PaymentInfo for event $eventId")
        
        for (payment in payments) {
            android.util.Log.d("OrganizerPaymentVM", "Processing payment: ${payment.paymentId}, eventId: ${payment.eventId}, userId: ${payment.userId}, status: ${payment.status.name}")
            
            // Log specifically for REFUND_PENDING payments
            if (payment.status.name == "REFUND_PENDING") {
                android.util.Log.d("OrganizerPaymentVM", "*** Processing REFUND_PENDING payment: ${payment.paymentId}")
            }
            
            try {
                // Get user info (participant who made the payment)
                val user = getUserProfileUseCase(payment.userId)
                val userName = user?.fullName ?: "Unknown User"
                val email = user?.email ?: "N/A"
                val phone = user?.phone ?: "N/A"

                // Format date and time
                val date = Date(payment.createdAt)
                val paymentDate = dateFormat.format(date)
                val paymentTime = timeFormat.format(date)

                // Get event name
                val event = _events.value.find { it.eventId == eventId }
                val eventName = event?.name ?: "Unknown Event"

                // Format amount
                val amount = String.format(Locale.US, "RM %.2f", payment.amount)

                // Get ticket info - calculate from reservation if available
                val tickets = if (payment.reservationId != null) {
                    val reservation = reservationMap[payment.reservationId]
                    val totalSeats = (reservation?.rockZoneSeats ?: 0) + (reservation?.normalZoneSeats ?: 0)
                    if (totalSeats > 0) {
                        "$totalSeats ticket${if (totalSeats > 1) "s" else ""}"
                    } else {
                        "1 ticket"
                    }
                } else {
                    "1 ticket"
                }

                // Format status
                val status = when (payment.status.name) {
                    "SUCCESS" -> "Paid"
                    "REFUND_PENDING" -> {
                        android.util.Log.d("OrganizerPaymentVM", "Converting REFUND_PENDING to 'Refund Pending' for payment ${payment.paymentId}")
                        "Refund Pending"
                    }
                    "REFUNDED" -> "Refunded"
                    "FAILED" -> "Failed"
                    else -> {
                        android.util.Log.w("OrganizerPaymentVM", "Unknown payment status: ${payment.status.name}, using as-is")
                        payment.status.name
                    }
                }
                
                android.util.Log.d("OrganizerPaymentVM", "Payment ${payment.paymentId} status: ${payment.status.name} -> $status")

                // Get refund request data from Firestore if payment has refund status
                var refundReason: String? = null
                var refundNotes: String? = null
                var refundRequestTime: String? = null
                
                if (payment.status.name == "REFUND_PENDING" || payment.status.name == "REFUNDED") {
                    try {
                        val paymentDoc = firestore.collection("payments").document(payment.paymentId).get().await()
                        if (paymentDoc.exists()) {
                            val data = paymentDoc.data
                            refundReason = data?.get("refundReason")?.toString()
                            refundNotes = data?.get("refundNotes")?.toString()
                            val refundRequestTimestamp = data?.get("refundRequestTime") as? Long
                            refundRequestTimestamp?.let {
                                refundRequestTime = dateFormat.format(Date(it))
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("OrganizerPaymentVM", "Error loading refund request data: ${e.message}", e)
                    }
                }

                val paymentInfo = PaymentInfo(
                    eventName = eventName,
                    userName = userName,
                    amount = amount,
                    tickets = tickets,
                    orderId = payment.paymentId,
                    email = email,
                    phone = phone,
                    paymentDate = paymentDate,
                    paymentTime = paymentTime,
                    status = status,
                    refundRequestTime = refundRequestTime,
                    refundReason = refundReason,
                    refundNotes = refundNotes,
                    refundStatus = if (status == "Refunded") "Approved" else null
                )
                
                paymentInfoList.add(paymentInfo)
                
                android.util.Log.d("OrganizerPaymentVM", "Added PaymentInfo for payment ${payment.paymentId}, userName: $userName, amount: $amount, status: $status")
                
                // Log specifically for REFUND_PENDING payments
                if (status == "Refund Pending") {
                    android.util.Log.d("OrganizerPaymentVM", "*** Successfully added REFUND_PENDING PaymentInfo: ${payment.paymentId}, status=$status, refundReason=$refundReason")
                }
            } catch (e: Exception) {
                // Skip this payment if there's an error
                android.util.Log.e("OrganizerPaymentVM", "Error converting payment ${payment.paymentId}: ${e.message}", e)
                continue
            }
        }

        android.util.Log.d("OrganizerPaymentVM", "Converted ${paymentInfoList.size} payments to PaymentInfo")
        return paymentInfoList
    }

    fun approveRefund(paymentId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = approveRefundUseCase(paymentId)
                if (success) {
                    // Reload payments to reflect the status change
                    val currentEventId = _paymentInfoList.value.firstOrNull()?.let { payment ->
                        _events.value.find { it.name == payment.eventName }?.eventId
                    }
                    currentEventId?.let { loadPaymentsForEvent(it) }
                    onSuccess()
                } else {
                    val errorMsg = "Failed to approve refund. Please try again."
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error approving refund: ${e.message}"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectRefund(paymentId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // For now, rejection means changing status back to SUCCESS (Paid)
                // This could be implemented as a separate use case if needed
                // For simplicity, we'll just show a message
                android.util.Log.d("OrganizerPaymentVM", "Refund rejection requested for payment: $paymentId")
                // TODO: Implement reject refund logic if needed
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Error rejecting refund: ${e.message}"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

