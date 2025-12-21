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
import com.example.sera_application.utils.BottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.presentation.viewmodel.payment.OrganizerPaymentManagementViewModel
import com.example.sera_application.domain.model.enums.UserRole
import com.example.sera_application.domain.model.ReservationWithDetails
import dagger.hilt.android.AndroidEntryPoint

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
    navController: androidx.navigation.NavController? = null,
    viewModel: OrganizerPaymentManagementViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedEventName by rememberSaveable { mutableStateOf("All Events") }
    var isDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var showReviewRefundDialog by rememberSaveable { mutableStateOf(false) }
    var showPaymentDetailsDialog by rememberSaveable { mutableStateOf(false) }
    var selectedReservation by remember { mutableStateOf<ReservationWithDetails?>(null) }
    
    // Get current user for role-based navigation
    val currentUser by profileViewModel.user.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

    // Load data when user is available
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userId ->
            viewModel.loadOrganizerData(userId)
        }
    }

    val eventNames: List<String> = remember(events) {
        listOf("All Events") + events.map { it.name }
    }

    // Filter payments based on selected event
    val filteredReservations: List<ReservationWithDetails> = remember(reservations, selectedEventName) {
        if (selectedEventName == "All Events") {
            reservations
        } else {
            reservations.filter { it.event?.name == selectedEventName }
        }
    }

    // Calculations based on filtered data
    val successful = filteredReservations.count { it.reservation.status == com.example.sera_application.domain.model.enums.ReservationStatus.CONFIRMED }
    val pendingRefunds = filteredReservations.count { it.reservation.status == com.example.sera_application.domain.model.enums.ReservationStatus.PENDING }
    val refunded = filteredReservations.count { it.reservation.status == com.example.sera_application.domain.model.enums.ReservationStatus.CANCELLED }
    val totalRevenue = filteredReservations
        .filter { it.reservation.status == com.example.sera_application.domain.model.enums.ReservationStatus.CONFIRMED }
        .sumOf { it.reservation.totalPrice }

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
            // Event Selector Card
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
                            value = selectedEventName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Event") },
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
                            eventNames.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedEventName = name
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading && filteredReservations.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = "Payment Summary",
                    fontSize = 20.sp,
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
                        PaymentSummaryRow("Successful (Confirmed)", successful.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentSummaryRow("Pending", pendingRefunds.toString())
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentSummaryRow("Total Revenue", String.format(Locale.US, "RM %.2f", totalRevenue))
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentSummaryRow("Cancelled/Refunded", refunded.toString())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment List",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredReservations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reservations found", color = Color.Gray)
                    }
                } else {
                    filteredReservations.forEach { resWithDetails ->
                        ReservationPaymentCard(
                            reservationWithDetails = resWithDetails,
                            onViewDetails = {
                                selectedReservation = resWithDetails
                                showPaymentDetailsDialog = true
                            },
                            onReviewRefund = {
                                selectedReservation = resWithDetails
                                showReviewRefundDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    if (showReviewRefundDialog && selectedReservation != null) {
        ReviewRefundDialog(
            reservationWithDetails = selectedReservation!!,
            onDismiss = { showReviewRefundDialog = false },
            onApprove = {
                viewModel.updateRefundStatus(selectedReservation!!.reservation.reservationId, true)
                showReviewRefundDialog = false
                Toast.makeText(context, "Refund Approved (Cancelled)", Toast.LENGTH_SHORT).show()
            },
            onReject = {
                viewModel.updateRefundStatus(selectedReservation!!.reservation.reservationId, false)
                showReviewRefundDialog = false
                Toast.makeText(context, "Refund Rejected (Confirmed)", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPaymentDetailsDialog && selectedReservation != null) {
        PaymentDetailsDialog(
            reservationWithDetails = selectedReservation!!,
            onDismiss = { showPaymentDetailsDialog = false }
        )
    }
}

@Composable
private fun ReservationPaymentCard(
    reservationWithDetails: ReservationWithDetails,
    onViewDetails: () -> Unit,
    onReviewRefund: () -> Unit
) {
    val reservation = reservationWithDetails.reservation
    val event = reservationWithDetails.event

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "ID: ${reservation.reservationId.takeLast(8)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = reservation.status.label,
                    color = reservation.status.color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(text = event?.name ?: "Unknown Event", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Amount: RM ${String.format("%.2f", reservation.totalPrice)}", fontSize = 14.sp)
            Text(text = "Seats: ${reservation.seats} (${reservation.rockZoneSeats} Rock, ${reservation.normalZoneSeats} Normal)", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (reservation.status == com.example.sera_application.domain.model.enums.ReservationStatus.PENDING) {
                    Button(
                        onClick = onReviewRefund,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                    ) {
                        Text("Review", color = Color.White)
                    }
                }
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details")
                }
            }
        }
    }
}

@Composable
fun ReviewRefundDialog(
    reservationWithDetails: ReservationWithDetails,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val reservation = reservationWithDetails.reservation
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review Refund Request") },
        text = {
            Column {
                Text("Reservation ID: ${reservation.reservationId}")
                Text("Event: ${reservationWithDetails.event?.name}")
                Text("Amount: RM ${String.format("%.2f", reservation.totalPrice)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Approving will set status to CANCELLED and rejecting will set it to CONFIRMED.")
            }
        },
        confirmButton = {
            Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Approve Refund")
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("Reject Refund")
            }
        }
    )
}

@Composable
fun PaymentDetailsDialog(
    reservationWithDetails: ReservationWithDetails,
    onDismiss: () -> Unit
) {
    val reservation = reservationWithDetails.reservation
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Details") },
        text = {
            Column {
                DetailRow("Event", reservationWithDetails.event?.name ?: "Unknown")
                DetailRow("Reservation ID", reservation.reservationId)
                DetailRow("User ID", reservation.userId)
                DetailRow("Total Price", "RM ${String.format("%.2f", reservation.totalPrice)}")
                DetailRow("Status", reservation.status.label)
                DetailRow("Created At", com.example.sera_application.utils.DateTimeFormatterUtil.formatDate(reservation.createdAt))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}