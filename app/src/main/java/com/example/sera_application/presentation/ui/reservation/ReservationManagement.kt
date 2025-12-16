package com.example.sera_application.presentation.ui.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReservationManagementUiModel(
    val reservationId: String,
    val eventName: String,
    val eventId: String,
    val dateTime: String,
    val status: ReservationStatus
)

enum class FilterStatus(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationManagementScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onViewReservation: (String) -> Unit = {},
    onExportParticipantList: () -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedEventFilter by rememberSaveable  { mutableStateOf("All Events") }
    var selectedStatusFilter by rememberSaveable  { mutableStateOf(FilterStatus.ALL) }
    var showEventDropdown by rememberSaveable  { mutableStateOf(false) }

    // TODO: replace with real data from DB
    val allReservations = remember {
        listOf(
            ReservationManagementUiModel(
                reservationId = "xxx123",
                eventName = "Music Fiesta 6.0",
                eventId = "EV001",
                dateTime = "Nov 16,2025, 7.00PM",
                status = ReservationStatus.CONFIRMED
            ),
            ReservationManagementUiModel(
                reservationId = "xxx124",
                eventName = "Music Fiesta 6.0",
                eventId = "EV001",
                dateTime = "Nov 16,2025, 7.00PM",
                status = ReservationStatus.PENDING
            ),
            ReservationManagementUiModel(
                reservationId = "xxx125",
                eventName = "Music Fiesta 6.0",
                eventId = "EV001",
                dateTime = "Nov 15,2025, 6.00PM",
                status = ReservationStatus.COMPLETED
            ),
            ReservationManagementUiModel(
                reservationId = "xxx126",
                eventName = "GOTAR Festival",
                eventId = "EV002",
                dateTime = "Nov 20,2025, 8.00PM",
                status = ReservationStatus.CANCELLED
            )
        )
    }

    // Filter reservations based on search and filters
    val filteredReservations = remember(
        searchQuery,
        selectedEventFilter,
        selectedStatusFilter
    ) {
        allReservations.filter { reservation ->
            // Search filter
            val matchesSearch = searchQuery.isEmpty() ||
                    reservation.eventName.contains(searchQuery, ignoreCase = true) ||
                    reservation.reservationId.contains(searchQuery, ignoreCase = true)

            // Event filter
            val matchesEvent = selectedEventFilter == "All Events" ||
                    reservation.eventName == selectedEventFilter

            // Status filter
            val matchesStatus = selectedStatusFilter == FilterStatus.ALL ||
                    (selectedStatusFilter == FilterStatus.PENDING && reservation.status == ReservationStatus.PENDING) ||
                    (selectedStatusFilter == FilterStatus.CONFIRMED && reservation.status == ReservationStatus.CONFIRMED) ||
                    (selectedStatusFilter == FilterStatus.CANCELLED && reservation.status == ReservationStatus.CANCELLED) ||
                    (selectedStatusFilter == FilterStatus.COMPLETED && reservation.status == ReservationStatus.COMPLETED)

            matchesSearch && matchesEvent && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Reservation Management",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
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
                    containerColor = Color(0xFF2C2C2E) // Dark grey header
                )
            )
        },
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Export Button (fixed at bottom)
            Button(
                onClick = onExportParticipantList,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2), // Blue
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Export Participant List",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Light grey background
        ) {
            // Search and Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF757575)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                // Event Filter Dropdown
                Box {
                    OutlinedButton(
                        onClick = { showEventDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(
                            selectedEventFilter,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Black
                        )
                    }

                    DropdownMenu(
                        expanded = showEventDropdown,
                        onDismissRequest = { showEventDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Events") },
                            onClick = {
                                selectedEventFilter = "All Events"
                                showEventDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Music Fiesta 6.0") },
                            onClick = {
                                selectedEventFilter = "Music Fiesta 6.0"
                                showEventDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("GOTAR Festival") },
                            onClick = {
                                selectedEventFilter = "GOTAR Festival"
                                showEventDropdown = false
                            }
                        )
                    }
                }

                // Status Filter Bar
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterStatus.entries.forEach { status ->
                        StatusFilterChip(
                            status = status,
                            isSelected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                        )
                    }
                }
            }

            // Reservation List
            if (filteredReservations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No reservations found",
                        fontSize = 16.sp,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredReservations,
                        key = { it.reservationId }
                    ) { reservation ->
                        ReservationManagementCard(
                            reservation = reservation,
                            onViewClick = { onViewReservation(reservation.reservationId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterChip(
    status: FilterStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1976D2) else Color.White,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        } else null
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color(0xFF757575),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReservationManagementCard(
    reservation: ReservationManagementUiModel,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .heightIn(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Row: Event Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = reservation.eventName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    // modifier = Modifier.weight(1f),
                    //overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reservation.status.label,
                    color = reservation.status.color,    // your existing color mapping
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Reservation ID
            Text(
                text = "Reservation ID: ${reservation.reservationId}",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )

            // Date and Time
            Text(
                text = reservation.dateTime,
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )

            // View Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onViewClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD8E8FF), // Light blue
                        contentColor = Color(0xFF1B4FA0)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 6.dp
                    ),
                    modifier = Modifier
                ) {
                    Text(
                        "View",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        //  modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservationManagementScreenPreview() {
    MaterialTheme {
        ReservationManagementScreen()
    }
}

