package com.example.sera_application.presentation.ui.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.reservation.ReservationListViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.utils.DateTimeFormatterUtil

enum class ReservationTab(val label: String) {
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class ReservationStatus(val label: String, val color: Color) {
    CONFIRMED("Confirmed", Color(0xFF4CAF50)),
    COMPLETED("Completed", Color(0xFF2196F3)),
    CANCELLED("Cancelled", Color(0xFFF44336)),
    PENDING("Pending", Color(0xFFFFC107))
}

data class ReservationUiModel(
    val reservationId: String,
    val eventId: String,
    val eventName: String,
    val organizerName: String,
    val date: String,
    val eventDate: Long, // Event date timestamp for cancellation validation
    val tab: ReservationTab,
    val status: ReservationStatus,
    val paymentId: String? = null
) {
    // Check if cancellation is allowed (not within 24 hours before event)
    fun canCancel(): Boolean {
        val currentTime = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        val timeDifference = eventDate - currentTime
        return timeDifference >= twentyFourHours
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onViewDetails: (ReservationUiModel) -> Unit = {},
    onCancelReservation: (ReservationUiModel) -> Unit = {},
    viewModel: ReservationListViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(ReservationTab.UPCOMING) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var reservationToCancel by remember { mutableStateOf<ReservationUiModel?>(null) }
    
    val currentUser by profileViewModel.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load current user on first composition
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

    // Fetch reservations when user is loaded
    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userId ->
            viewModel.fetchReservations(userId)
        }
    }

    val reservationDetailsList by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCancelling by viewModel.isCancelling.collectAsState()
    val cancelError by viewModel.error.collectAsState()
    val cancelSuccess by viewModel.cancelSuccess.collectAsState()

    // Show error snackbar
    LaunchedEffect(cancelError) {
        cancelError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // Show success snackbar
    LaunchedEffect(cancelSuccess) {
        cancelSuccess?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearSuccess()
        }
    }

    // Map domain model to UI model
    val allReservations = remember(reservationDetailsList) {
        reservationDetailsList.map { details ->
            ReservationUiModel(
                reservationId = details.reservation.reservationId,
                eventId = details.reservation.eventId,
                eventName = details.event?.name ?: "Unknown Event",
                organizerName = details.event?.organizerName ?: "Unknown Organizer",
                date = details.event?.date
                    ?.let { DateTimeFormatterUtil.formatDate(it) }
                    ?: "Unknown Date",
                eventDate = details.event?.date ?: 0L, // Add event timestamp
                // Logic to determine tab based on status or date
                tab = when (details.reservation.status) {
                    com.example.sera_application.domain.model.enums.ReservationStatus.CONFIRMED -> ReservationTab.UPCOMING
                    com.example.sera_application.domain.model.enums.ReservationStatus.PENDING -> ReservationTab.UPCOMING
                    com.example.sera_application.domain.model.enums.ReservationStatus.COMPLETED -> ReservationTab.COMPLETED
                    com.example.sera_application.domain.model.enums.ReservationStatus.CANCELLED -> ReservationTab.CANCELLED
                    else -> ReservationTab.UPCOMING
                },
                status = when(details.reservation.status) {
                    com.example.sera_application.domain.model.enums.ReservationStatus.CONFIRMED -> ReservationStatus.CONFIRMED
                    com.example.sera_application.domain.model.enums.ReservationStatus.PENDING -> ReservationStatus.PENDING
                    com.example.sera_application.domain.model.enums.ReservationStatus.CANCELLED -> ReservationStatus.CANCELLED
                    com.example.sera_application.domain.model.enums.ReservationStatus.COMPLETED -> ReservationStatus.COMPLETED
                    else -> ReservationStatus.PENDING
                },
                paymentId = details.paymentId
            )
        }
    }

    val reservations = allReservations.filter { it.tab == selectedTab }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Reservation",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2C2C2E)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (showCancelDialog && reservationToCancel != null) {
            EnhancedCancelDialog(
                reservation = reservationToCancel!!,
                isCancelling = isCancelling,
                onDismiss = { showCancelDialog = false },
                onConfirm = {
                    currentUser?.userId?.let { userId ->
                        viewModel.cancelReservation(reservationToCancel!!.reservationId, userId)
                    }
                    showCancelDialog = false
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Using a single LazyColumn to avoid nesting scrollable components
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF4F4F4)),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
            // Header / Tabs section
            item {
                ReservationTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            if (reservations.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reservations in this category.")
                    }
                }
            } else {
                items(
                    items = reservations,
                    key = { it.reservationId }
                ) { item ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ReservationCard(
                            reservation = item,
                            onViewDetails = { onViewDetails(item) },
                            onCancelReservation = {
                                reservationToCancel = item
                                showCancelDialog = true
                            }
                        )
                    }
                }
            }
        }

            // Loading overlay during cancellation
            if (isCancelling) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                "Cancelling reservation...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationTabs(
    selectedTab: ReservationTab,
    onTabSelected: (ReservationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFE8ECF4)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReservationTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                Surface(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF1F7AE0) else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Color(0xFF7B8894),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: ReservationUiModel,
    onViewDetails: () -> Unit,
    onCancelReservation: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            // Top row: Event id + date
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE4FAD2), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.eventId,
                    fontSize = 12.sp,
                    color = Color(0xFF5F6B76)
                )
                Text(
                    text = reservation.date,
                    fontSize = 12.sp,
                    color = Color(0xFF5F6B76)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Event name + status pill
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reservation.eventName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = reservation.organizerName,
                        fontSize = 12.sp,
                        color = Color(0xFF808D9A)
                    )
                }

                StatusChip(reservation.status)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("View Details", fontSize = 13.sp)
                }

                // Show cancel button based on status and 24-hour policy
                val canCancelStatus = reservation.status == ReservationStatus.CONFIRMED ||
                                     reservation.status == ReservationStatus.PENDING
                val canCancelTime = reservation.canCancel()
                
                if (canCancelStatus) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (canCancelTime) {
                                onCancelReservation()
                            }
                        },
                        enabled = canCancelTime,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canCancelTime) Color(0xFFFFF1F1) else Color(0xFFE0E0E0),
                            contentColor = if (canCancelTime) Color(0xFFE53935) else Color(0xFF9E9E9E),
                            disabledContainerColor = Color(0xFFE0E0E0),
                            disabledContentColor = Color(0xFF9E9E9E)
                        )
                    ) {
                        Text(
                            text = if (canCancelTime) "Cancel Reservation" else "Cannot Cancel",
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onViewDetails,
                        enabled = false,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Cancel Reservation", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: ReservationStatus) {
    Surface(
        shape = CircleShape,
        color = status.color.copy(alpha = 0.12f),
        contentColor = status.color
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EnhancedCancelDialog(
    reservation: ReservationUiModel,
    isCancelling: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF1F1),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        title = {
            Text(
                "Cancel Reservation?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
        },
        text = {
            // Make dialog content scrollable for landscape mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit height for landscape
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reservation Details
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Event:",
                                fontSize = 13.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                reservation.eventName,
                                fontSize = 13.sp,
                                color = Color(0xFF1C1B1F),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, false)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Date:",
                                fontSize = 13.sp,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                reservation.date,
                                fontSize = 13.sp,
                                color = Color(0xFF1C1B1F)
                            )
                        }
                    }
                }

                // Warning Message
                Text(
                    "Are you sure you want to cancel this reservation?",
                    fontSize = 14.sp,
                    color = Color(0xFF1C1B1F),
                    lineHeight = 20.sp
                )

                // Refund Information
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "ℹ️",
                            fontSize = 16.sp
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Refund Information",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                "Your refund will be processed within 3-5 business days to your original payment method.",
                                fontSize = 11.sp,
                                color = Color(0xFF424242),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                if (reservation.paymentId != null) {
                    Text(
                        "Payment ID: ${reservation.paymentId}",
                        fontSize = 11.sp,
                        color = Color(0xFF888888),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isCancelling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(42.dp)
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (isCancelling) "Cancelling..." else "Yes, Cancel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isCancelling,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Text(
                    "Keep Reservation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
private fun MyReservationScreenPreview() {
    MaterialTheme {
        MyReservationScreen()
    }
}