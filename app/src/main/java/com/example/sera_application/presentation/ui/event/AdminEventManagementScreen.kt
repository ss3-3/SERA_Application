package com.example.sera_application.presentation.ui.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.presentation.viewmodel.event.AdminEventManagementViewModel

// UI model for admin event list
data class AdminEventModel(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val status: EventStatus,
    val bannerUrl: String? = null
)

// UI model for admin event approval details
data class AdminEventDetails(
    val id: String,
    val name: String,
    val description: String,
    val rockZoneSeats: String,
    val normalZoneSeats: String,
    val date: String,
    val duration: String,
    val startTime: String,
    val endTime: String,
    val venue: String,
    val organizer: String,
    val bannerUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventManagementScreen(
    onEventClick: (String) -> Unit = {},  // Navigate to approval screen
    onDeleteEventClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onReservationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: AdminEventManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatus by remember { mutableStateOf<EventStatus?>(EventStatus.PENDING) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Load events on first composition
    LaunchedEffect(Unit) {
        viewModel.loadAllEvents()
    }

    // Update ViewModel when search query or status changes
    LaunchedEffect(selectedStatus) {
        viewModel.updateStatusFilter(selectedStatus)
    }

    // Get filtered events from ViewModel
    val filteredEvents = viewModel.getFilteredEvents()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // TARUMT Logo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "TARUMT Logo",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "TARUMT",
                                fontSize = 8.sp,
                                color = Color(0xFFE91E63),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Search bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search", fontSize = 12.sp, color = Color.LightGray.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray)
                            },
                            modifier = Modifier
                                .width(375.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(13.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.LightGray,
                                unfocusedTextColor = Color.LightGray,
                                cursorColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onHomeClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Event, contentDescription = "Event") },
                    label = { Text("Event") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BookOnline, contentDescription = "Reservation") },
                    label = { Text("Reservation") },
                    selected = false,
                    onClick = onReservationClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Me") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // Status Filter Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Choose By Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(EventStatus.values().toList()) { status ->
                        StatusFilterChip(
                            status = status,
                            isSelected = selectedStatus == status,
                            onClick = { selectedStatus = status }
                        )
                    }
                }
                
                // Show loading or error state
                if (uiState.isLoading && filteredEvents.isEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading events...", color = Color.Gray)
                    }
                }
                
                if (uiState.errorMessage != null && filteredEvents.isEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Error loading events",
                            color = Color.Red
                        )
                    }
                }
            }

            // Event List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredEvents) { event ->
                    AdminEventCard(
                        event = event,
                        onManageClick = { onEventClick(event.id) },
                        onDeleteClick = {
                            showDeleteDialog = event.id
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Delete Confirmation Dialog
            showDeleteDialog?.let { eventId ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Delete Event") },
                    text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteEvent(eventId) { success, error ->
                                    showDeleteDialog = null
                                    if (!success) {
                                        // Error is shown in UI state
                                    }
                                }
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusFilterChip(
    status: EventStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> when (status) {
            EventStatus.APPROVED -> Color(0xFFE3FDE3)
            EventStatus.PENDING -> Color(0xFFE3F2FD)
            EventStatus.REJECTED -> Color(0xFFFFEBEE)
            EventStatus.COMPLETED -> Color(0xFFFFF3E0)
            EventStatus.CANCELLED -> Color(0xFFEEEEEE)
        }
        else -> Color(0xFFF5F5F5)
    }

    val textColor = when {
        isSelected -> when (status) {
            EventStatus.APPROVED -> Color(0xFF4CAF50)
            EventStatus.PENDING -> Color(0xFF2196F3) // orange: 0xFFFF9800
            EventStatus.REJECTED -> Color(0xFFE91E63)
            EventStatus.COMPLETED -> Color(0xFFFF9800)
            EventStatus.CANCELLED -> Color.Gray
        }
        else -> Color.Gray
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun AdminEventCard(
    event: AdminEventModel,
    onManageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.height(150.dp).width(190.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                val context = LocalContext.current
                val imageRes = remember(event.bannerUrl) {
                    if (event.bannerUrl != null && event.bannerUrl.isNotBlank()) {
                        context.resources.getIdentifier(
                            event.bannerUrl,
                            "drawable",
                            context.packageName
                        )
                    } else {
                        0
                    }
                }
                // Event Banner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1A2E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageRes != 0) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = event.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // fallback (same as before)
                        Text(
                            text = event.name.take(12),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

//            Spacer(modifier = Modifier.width(15.dp))

            // Event Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = event.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.date}, ${event.time}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))

                    // Status Badge
                    Text(
                        text = "Status: ${event.status.name}",
                        fontSize = 11.sp,
                        color = when (event.status) {
                            EventStatus.APPROVED -> Color(0xFF4CAF50)
                            EventStatus.PENDING -> Color(0xFF2196F3) // 0xFFFF9800
                            EventStatus.REJECTED -> Color(0xFFE91E63)
                            EventStatus.COMPLETED -> Color(0xFFFF9800)
                            EventStatus.CANCELLED -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Manage Button
                    Button(
                        onClick = onManageClick,
                        modifier = Modifier.height(34.dp).width(125.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                    ) {
                        Text(
                            text = "MANAGE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(35.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminEventManagementScreenPreview() {
    AdminEventManagementScreen()
}