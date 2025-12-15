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
import androidx.compose.material3.*
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

class OrganizerPaymentManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SERA_ApplicationTheme {
                OrganizerPaymentManagementScreen()
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
fun OrganizerPaymentManagementScreen() {
    val context = LocalContext.current
    var selectedEvent by rememberSaveable { mutableStateOf("") }
    var isDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var showReviewRefundDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentDetailsDialog by rememberSaveable { mutableStateOf(false) }
    var selectedPayment by rememberSaveable { mutableStateOf<PaymentInfo?>(null) }

    val events = listOf("MUSIC FIESTA 6.0", "GUITAR Festival", "VOICHESTRA")

    val allPayments = remember {
        mutableStateListOf(
            PaymentInfo(
                eventName = "MUSIC FIESTA 6.0",
                userName = "Lim Siau Siau",
                amount = "RM 70.00",
                tickets = "2 tickets",
                orderId = "1234-1234-1234",
                email = "lim@gmail.com",
                phone = "+60 123456789",
                paymentDate = "Jan 12, 2025",
                paymentTime = "3:00 PM",
                status = "Refund Pending",
                refundRequestTime = "2 hours ago",
                refundReason = "Cannot attend the event"
            ),
            PaymentInfo(
                eventName = "MUSIC FIESTA 6.0",
                userName = "John Doe",
                amount = "RM 35.00",
                tickets = "1 ticket",
                orderId = "5678-5678-5678",
                email = "john@gmail.com",
                phone = "+60 987654321",
                paymentDate = "Jan 10, 2025",
                paymentTime = "2:00 PM",
                status = "Refunded",
                refundRequestTime = "5 days ago",
                refundReason = "Other (please specify)",
                refundNotes = "I have a family emergency and need to travel urgently.",
                refundStatus = "Approved"
            ),
            PaymentInfo(
                eventName = "GUITAR Festival",
                userName = "Jane Smith",
                amount = "RM 105.00",
                tickets = "3 tickets",
                orderId = "9012-9012-9012",
                email = "jane@gmail.com",
                phone = "+60 111222333",
                paymentDate = "Jan 8, 2025",
                paymentTime = "10:00 AM",
                status = "Refund Pending",
                refundRequestTime = "1 day ago",
                refundReason = "Event details changed"
            ),
            PaymentInfo(
                eventName = "VOICHESTRA",
                userName = "Alice Wong",
                amount = "RM 180.00",
                tickets = "4 tickets",
                orderId = "3456-3456-3456",
                email = "alice@gmail.com",
                phone = "+60 444555666",
                paymentDate = "Jan 5, 2025",
                paymentTime = "11:00 AM",
                status = "Refunded",
                refundRequestTime = "3 days ago",
                refundReason = "Personal reasons",
                refundStatus = "Approved"
            )
        )
    }

    val filteredPayments = if (selectedEvent.isEmpty()) {
       emptyList()
    } else {
        allPayments.filter { it.eventName == selectedEvent }
    }

    val successful = filteredPayments.count { it.status == "Paid" }
    val pendingRefunds = filteredPayments.count { it.status == "Refund Pending" }
    val refunded = filteredPayments.count { it.status == "Refunded" }
    val totalRevenue = filteredPayments
        .filter { it.status == "Paid" }
        .sumOf { it.amount.replace("RM ", "").toDoubleOrNull() ?: 0.0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Management",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            OrganizerBottomNavigationBar()
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
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = it }
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
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5B9FED),
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            events.forEach { event ->
                                DropdownMenuItem(
                                    text = { Text(event) },
                                    onClick = {
                                        selectedEvent = event
                                        isDropdownExpanded = false
                                    },
                                    modifier = Modifier.background(
                                        if (event == selectedEvent) Color(0xFFF0F0F0) else Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (selectedEvent.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

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

                if (filteredPayments.isEmpty()) {
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
                            Text(
                                text = "No payments found",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
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
                val index = allPayments.indexOf(selectedPayment)
                if (index != -1) {
                    allPayments[index] = allPayments[index].copy(
                        refundStatus = "Approved",
                        status = "Refunded"
                    )
                }
                Toast.makeText(context, "Refund approved", Toast.LENGTH_SHORT).show()
                showReviewRefundDialog = false
            },
            onReject = {
                val index = allPayments.indexOf(selectedPayment)
                if (index != -1) {
                    allPayments[index] = allPayments[index].copy(
                        refundStatus = "Rejected",
                        status = "Paid"
                    )
                }
                Toast.makeText(context, "Refund rejected", Toast.LENGTH_SHORT).show()
                showReviewRefundDialog = false
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

@Composable
fun OrganizerBottomNavigationBar() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        Toast.makeText(context, "Navigate to Home", Toast.LENGTH_SHORT).show()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Home",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        Toast.makeText(context, "Add Event", Toast.LENGTH_SHORT).show()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        Toast.makeText(context, "Navigate to Profile", Toast.LENGTH_SHORT).show()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Profile",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}