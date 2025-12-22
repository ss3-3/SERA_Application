package com.example.sera_application.presentation.ui.reservation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.presentation.viewmodel.reservation.AdminReservationViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.utils.DateTimeFormatterUtil
import com.example.sera_application.utils.BottomNavigationBar

data class AdminReservationUiModel(
    val reservationId: String,
    val participantName: String,
    val participantId: String,
    val eventName: String,
    val eventId: String,
    val status: ReservationStatus,
    val dateTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReservationManagementScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: AdminReservationViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf<ReservationStatus?>(null) }
    
    // Get current user for role-based navigation
    val currentUser by profileViewModel.user.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }

    // Load all reservations on first composition
    LaunchedEffect(Unit) {
        viewModel.loadAllReservations()
    }

    val reservationList by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Map to UI model
    val allReservations = remember(reservationList) {
        reservationList.map { details ->
            AdminReservationUiModel(
                reservationId = details.reservation.reservationId,
                participantName = details.user?.fullName ?: "Unknown User",
                participantId = details.reservation.userId,
                eventName = details.event?.name ?: "Unknown Event",
                eventId = details.reservation.eventId,
                status = details.reservation.status,
                dateTime = details.event?.let { event ->
                    val date = DateTimeFormatterUtil.formatDate(event.date)
                    val time = DateTimeFormatterUtil.formatTime(event.startTime)
                    "$date, $time"
                } ?: "Unknown Date"
            )
        }
    }

    // Filter reservations based on search and status
    val filteredReservations = remember(allReservations, searchQuery, selectedStatus) {
        allReservations.filter { reservation ->
            val matchesSearch = searchQuery.isEmpty() ||
                    reservation.reservationId.contains(searchQuery, ignoreCase = true) ||
                    reservation.participantName.contains(searchQuery, ignoreCase = true) ||
                    reservation.eventName.contains(searchQuery, ignoreCase = true)
            
            val matchesStatus = selectedStatus == null || reservation.status == selectedStatus
            
            matchesSearch && matchesStatus
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Reservation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2C2C2E)
                )
            )
        },
        bottomBar = {
            navController?.let { nav ->
                BottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search by id, event name, user name",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status Filter Section
                Text(
                    text = "Filter By Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // All Status chip
                    item {
                        StatusFilterChip(
                            label = "All",
                            isSelected = selectedStatus == null,
                            onClick = { selectedStatus = null },
                            color = Color(0xFF2196F3)
                        )
                    }
                    
                    // Individual status chips
                    items(ReservationStatus.values().toList()) { status ->
                        StatusFilterChip(
                            label = status.name.lowercase().replaceFirstChar { it.uppercase() },
                            isSelected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            color = when (status) {
                                ReservationStatus.CONFIRMED -> Color(0xFF4CAF50)
                                ReservationStatus.PENDING -> Color(0xFFFF9800)
                                ReservationStatus.CANCELLED -> Color(0xFFE91E63)
                                ReservationStatus.COMPLETED -> Color(0xFF2196F3)
                                ReservationStatus.EXPIRED -> Color(0xFF757575)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
                filteredReservations.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No reservations found" 
                                   else "No matching reservations",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredReservations,
                            key = { it.reservationId }
                        ) { reservation ->
                            AdminReservationCard(reservation = reservation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReservationCard(
    reservation: AdminReservationUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Reservation ID label and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Actual Reservation ID
                Text(
                    text = reservation.reservationId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (reservation.status) {
                        ReservationStatus.CONFIRMED -> Color(0xFF4CAF50)
                        ReservationStatus.CANCELLED -> Color(0xFFE91E63)
                        ReservationStatus.PENDING -> Color(0xFFFF9800)
                        ReservationStatus.COMPLETED -> Color(0xFF2196F3)
                        ReservationStatus.EXPIRED -> Color(0xFF757575)
                    }
                ) {
                    Text(
                        text = reservation.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Participant Name
            Text(
                text = reservation.participantName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Event Name
            Text(
                text = reservation.eventName,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date & Time
            Text(
                text = reservation.dateTime,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5)
    val textColor = if (isSelected) color else Color.Gray

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

