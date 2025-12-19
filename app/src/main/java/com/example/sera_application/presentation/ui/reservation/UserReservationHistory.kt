package com.example.sera_application.presentation.ui.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    val tab: ReservationTab,
    val status: ReservationStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onViewDetails: (ReservationUiModel) -> Unit = {},
    onCancelReservation: (ReservationUiModel) -> Unit = {},
    viewModel: com.example.sera_application.presentation.viewmodel.reservation.ReservationListViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(ReservationTab.UPCOMING) }
    
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            viewModel.fetchReservations(currentUserId)
        }
    }

    val reservationDetailsList by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()

    // Map domain model to UI model
    val allReservations = remember(reservationDetailsList) {
        reservationDetailsList.map { details ->
            ReservationUiModel(
                reservationId = details.reservation.reservationId,
                eventId = details.reservation.eventId,
                eventName = details.event?.name ?: "Unknown Event",
                organizerName = details.event?.organizerId ?: "Unknown Organizer", // Ideal: Fetch organizer name too
                date = details.event?.date ?: "Unknown Date",
                // Logic to determine tab based on status or date
                tab = when (details.reservation.status) {
                     com.example.sera_application.domain.model.enums.ReservationStatus.CONFIRMED, 
                     com.example.sera_application.domain.model.enums.ReservationStatus.PENDING -> ReservationTab.UPCOMING // Simplification
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
                }
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
                    containerColor = Color(0xFF2C2C2E) // Dark grey header
                )
            )// Dark grey header
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFFF4F4F4))
        ) {
            ReservationTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (reservations.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("No reservations in this category.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = reservations,
                        key = { it.reservationId }
                    ) { item ->
                        ReservationCard(
                            reservation = item,
                            onViewDetails = { onViewDetails(item) },
                            onCancelReservation = { onCancelReservation(item) }
                        )
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
                        text = "Organizer Name",
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

                // Only show cancel button when applicable
                if (reservation.status == ReservationStatus.CONFIRMED ||
                    reservation.status == ReservationStatus.PENDING
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onCancelReservation,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF1F1),
                            contentColor = Color(0xFFE53935)
                        )
                    ) {
                        Text("Cancel Reservation", fontSize = 13.sp)
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

@Preview(showBackground = true)
@Composable
private fun MyReservationScreenPreview() {
    MaterialTheme {
        MyReservationScreen()
    }
}