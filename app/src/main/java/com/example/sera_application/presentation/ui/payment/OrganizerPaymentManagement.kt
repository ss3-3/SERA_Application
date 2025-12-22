package com.example.sera_application.presentation.ui.payment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sera_application.ui.theme.SERA_ApplicationTheme
import java.util.Locale
import com.example.sera_application.utils.BottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.presentation.viewmodel.payment.OrganizerPaymentManagementViewModel
import com.example.sera_application.domain.model.enums.UserRole
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import com.example.sera_application.utils.BottomNavigationBar

@AndroidEntryPoint
class OrganizerPaymentManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SERA_ApplicationTheme {
                OrganizerPaymentManagementScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

data class PaymentInfo(
    val eventName: String,
    val userName: String,
    val amount: String,
    val tickets: String,
    val orderId: String,
    val email: String,
    val phone: String,
    val paymentDate: String,
    val paymentTime: String,
    val status: String,
    val refundRequestTime: String?,
    val refundReason: String?,
    val refundNotes: String? = null,
    var refundStatus: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerPaymentManagementScreen(
    onBack: () -> Unit = {},
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    var selectedEvent by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showReviewRefundDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentDetailsDialog by rememberSaveable { mutableStateOf(false) }
    var selectedPayment by rememberSaveable { mutableStateOf<PaymentInfo?>(null) }
    var selectedStatusFilter by rememberSaveable { mutableStateOf("All") }

    // Get current user for role-based navigation
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()

    // Get payment management ViewModel
    val paymentViewModel: OrganizerPaymentManagementViewModel = hiltViewModel()
    val events by paymentViewModel.events.collectAsState()
    val paymentInfoList by paymentViewModel.paymentInfoList.collectAsState()
    val isLoading by paymentViewModel.isLoading.collectAsState()
    val error by paymentViewModel.error.collectAsState()

    // Refresh function
    fun refreshData() {
        paymentViewModel.loadOrganizerEvents()
        if (selectedEvent.isNotEmpty()) {
            val selectedEventObj = events.find { it.name == selectedEvent }
            selectedEventObj?.let {
                paymentViewModel.loadPaymentsForEvent(it.eventId)
            }
        }
    }

    // Get lifecycle owner for screen focus detection
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Refresh data when screen comes into focus (similar to onResume)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load current user and organizer's events
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
        paymentViewModel.loadOrganizerEvents()
    }

    // Load payments when event is selected
    LaunchedEffect(selectedEvent, events.size) {
        android.util.Log.d("OrganizerPaymentManagement", "LaunchedEffect triggered: selectedEvent=$selectedEvent, events.size=${events.size}")
        if (selectedEvent.isNotEmpty() && events.isNotEmpty()) {
            // Find event ID from selected event name
            val selectedEventObj = events.find { it.name == selectedEvent }
            android.util.Log.d("OrganizerPaymentManagement", "Selected event object: ${selectedEventObj?.name}, eventId: ${selectedEventObj?.eventId}")
            selectedEventObj?.let {
                android.util.Log.d("OrganizerPaymentManagement", "Loading payments for event: ${it.eventId}")
                paymentViewModel.loadPaymentsForEvent(it.eventId)
            }
        }
    }

    // Debug: Log payment info list changes
    LaunchedEffect(paymentInfoList.size) {
        android.util.Log.d("OrganizerPaymentManagement", "Payment info list size: ${paymentInfoList.size}")
        paymentInfoList.forEachIndexed { index, payment ->
            android.util.Log.d("OrganizerPaymentManagement", "Payment[$index]: ${payment.orderId}, ${payment.userName}, ${payment.amount}, ${payment.status}")
        }
    }

    // Get event names for dropdown - show all events sorted by name
    val eventNames = events.map { it.name }.sorted()

    // Debug: Log number of events loaded
    LaunchedEffect(events.size) {
        android.util.Log.d("OrganizerPaymentManagement", "Total events loaded: ${events.size}")
        android.util.Log.d("OrganizerPaymentManagement", "Event names: $eventNames")
    }

    // Filter payments by status
    val filteredPayments = remember(paymentInfoList, selectedStatusFilter) {
        if (selectedStatusFilter == "All") {
            paymentInfoList
        } else {
            paymentInfoList.filter { payment ->
                when (selectedStatusFilter) {
                    "Paid" -> payment.status == "Paid"
                    "Refund Pending" -> payment.status == "Refund Pending"
                    "Refunded" -> payment.status == "Refunded"
                    "Failed" -> payment.status == "Failed"
                    else -> true
                }
            }
        }
    }

    // Debug: Log payment statuses
    LaunchedEffect(paymentInfoList.size) {
        android.util.Log.d("OrganizerPaymentManagement", "Total payments in list: ${paymentInfoList.size}")
        paymentInfoList.forEach { payment ->
            android.util.Log.d("OrganizerPaymentManagement", "Payment: ${payment.orderId}, status: ${payment.status}, event: ${payment.eventName}")
            if (payment.status == "Refund Pending") {
                android.util.Log.d("OrganizerPaymentManagement", "*** REFUND_PENDING payment in UI: ${payment.orderId}")
            }
        }
    }

    val successful = filteredPayments.count { it.status == "Paid" }
    val pendingRefunds = filteredPayments.count { it.status == "Refund Pending" }
    val refunded = filteredPayments.count { it.status == "Refunded" }

    // Debug: Log counts
    LaunchedEffect(successful, pendingRefunds, refunded) {
        android.util.Log.d("OrganizerPaymentManagement", "Payment counts - Paid: $successful, Refund Pending: $pendingRefunds, Refunded: $refunded")
    }
    val totalRevenue = filteredPayments
        .filter { it.status == "Paid" }
        .sumOf { it.amount.replace("RM ", "").toDoubleOrNull() ?: 0.0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Payment Management",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshData() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            )
        },
        bottomBar = {
            navController?.let { nav ->
                BottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role ?: UserRole.ORGANIZER
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = if (selectedEvent.isEmpty()) "" else selectedEvent,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select the event") },
                            placeholder = { Text("Select the event") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5B9FED),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else if (eventNames.isEmpty()) {
                                    Text(
                                        text = "No events found",
                                        modifier = Modifier.padding(16.dp),
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    eventNames.forEach { eventName ->
                                        DropdownMenuItem(
                                            text = { Text(eventName) },
                                            onClick = {
                                                selectedEvent = eventName
                                                expanded = false
                                            },
                                            leadingIcon = if (eventName == selectedEvent) {
                                                {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color(0xFF4CAF50)
                                                    )
                                                }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedEvent.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                // Show which event is selected (for debugging)
                val selectedEventObj = events.find { it.name == selectedEvent }
                selectedEventObj?.let { event ->
                    Text(
                        text = "Event: ${event.name}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Event ID: ${event.eventId}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payment Summary",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        PaymentSummaryRow("Successful", successful.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentSummaryRow("Pending Refunds", pendingRefunds.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentSummaryRow("Total Revenue", String.format(Locale.US, "RM %.2f", totalRevenue))
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentSummaryRow("Refunded", refunded.toString())
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Payment List",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Filter Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Paid", "Refund Pending", "Refunded", "Failed").forEach { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text(status, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Show error message if there's an error
                if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Error Loading Payments",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = error ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                            Button(
                                onClick = { 
                                    selectedEventObj?.let { 
                                        paymentViewModel.loadPaymentsForEvent(it.eventId) 
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5B9FED)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isLoading) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredPayments.isEmpty() && error == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No payments found",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            selectedEventObj?.let { event ->
                                Text(
                                    text = "Event ID: ${event.eventId}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                                Text(
                                    text = "Total payments loaded: ${paymentInfoList.size}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                                if (selectedStatusFilter != "All") {
                                    Text(
                                        text = "Filter: $selectedStatusFilter",
                                        fontSize = 12.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                            Button(
                                onClick = { 
                                    selectedEventObj?.let { 
                                        paymentViewModel.loadPaymentsForEvent(it.eventId) 
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5B9FED)
                                ),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Refresh")
                            }
                        }
                    }
                } else {
                    filteredPayments.forEach { payment ->
                        PaymentCard(
                            paymentInfo = payment,
                            onReviewRefund = {
                                selectedPayment = payment
                                showReviewRefundDialog = true
                            },
                            onViewDetails = {
                                selectedPayment = payment
                                showPaymentDetailsDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (showReviewRefundDialog && selectedPayment != null) {
        ReviewRefundDialog(
            paymentInfo = selectedPayment!!,
            onDismiss = { showReviewRefundDialog = false },
            onApprove = {
                selectedPayment?.orderId?.let { paymentId ->
                    paymentViewModel.approveRefund(
                        paymentId = paymentId,
                        onSuccess = {
                            Toast.makeText(context, "Refund approved successfully", Toast.LENGTH_SHORT).show()
                            showReviewRefundDialog = false
                            refreshData()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                } ?: run {
                    Toast.makeText(context, "Invalid payment ID", Toast.LENGTH_SHORT).show()
                }
            },
            onReject = {
                selectedPayment?.orderId?.let { paymentId ->
                    paymentViewModel.rejectRefund(
                        paymentId = paymentId,
                        onSuccess = {
                            Toast.makeText(context, "Refund rejected", Toast.LENGTH_SHORT).show()
                            showReviewRefundDialog = false
                            refreshData()
                        },
                        onError = { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                } ?: run {
                    Toast.makeText(context, "Invalid payment ID", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showPaymentDetailsDialog && selectedPayment != null) {
        PaymentDetailsDialog(
            paymentInfo = selectedPayment!!,
            onDismiss = { showPaymentDetailsDialog = false }
        )
    }

}

@Composable
fun PaymentSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun PaymentCard(
    paymentInfo: PaymentInfo,
    onReviewRefund: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = paymentInfo.userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "${paymentInfo.amount} • ${paymentInfo.tickets}",
                fontSize = 14.sp,
                color = Color.Black
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = paymentInfo.status,
                    fontSize = 14.sp,
                    color = when (paymentInfo.status) {
                        "Paid" -> Color(0xFF4CAF50)
                        "Refund Pending" -> Color(0xFFFFA726)
                        "Refunded" -> Color(0xFF2196F3)
                        else -> Color.Black
                    },
                    fontWeight = FontWeight.Medium
                )

                if (paymentInfo.refundStatus != null) {
                    Text(
                        text = " • ${paymentInfo.refundStatus}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            if (paymentInfo.refundRequestTime != null) {
                Text(
                    text = "Refund requested ${paymentInfo.refundRequestTime}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }

            if (paymentInfo.refundReason != null) {
                Text(
                    text = "Reason: ${paymentInfo.refundReason}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )

                if (paymentInfo.refundNotes != null) {
                    Text(
                        text = "Notes: ${paymentInfo.refundNotes}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (paymentInfo.status == "Refund Pending") {
                    OutlinedButton(
                        onClick = onReviewRefund,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Review Refund", fontSize = 14.sp)
                    }
                }


                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text("View Details", fontSize = 14.sp)
                }
            }
        }
    }
}


@Composable
fun ReviewRefundDialog(
    paymentInfo: PaymentInfo,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Review Refund Request",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DetailRow("Event", paymentInfo.eventName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("User", paymentInfo.userName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Amount", paymentInfo.amount)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Order ID", paymentInfo.orderId)
                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = "Reason",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = paymentInfo.refundReason ?: "No reason provided",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )


                    if (paymentInfo.refundNotes != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Additional Notes",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = paymentInfo.refundNotes,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Approve")
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun PaymentDetailsDialog(
    paymentInfo: PaymentInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Payment Details",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DetailRow("Event", paymentInfo.eventName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Name", paymentInfo.userName)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Email", paymentInfo.email)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Phone", paymentInfo.phone)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Amount", paymentInfo.amount)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Tickets", paymentInfo.tickets)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Order ID", paymentInfo.orderId)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Payment Date", "${paymentInfo.paymentDate} ${paymentInfo.paymentTime}")
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow("Status", paymentInfo.status)

                if (paymentInfo.refundReason != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column {
                        Text(
                            text = "Refund Reason",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = paymentInfo.refundReason,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        if (paymentInfo.refundNotes != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Additional Notes",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = paymentInfo.refundNotes,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }

                if (paymentInfo.refundStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Refund Status", paymentInfo.refundStatus!!)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9FED)
                )
            ) {
                Text("Close")
            }
        },
        dismissButton = null
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}