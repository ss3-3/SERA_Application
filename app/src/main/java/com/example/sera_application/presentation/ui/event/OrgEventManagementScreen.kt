package com.example.sera_application.presentation.ui.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.presentation.viewmodel.event.OrganizerEventManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerEventManagementScreen(
    organizerId: String = "organizer_1", // TODO: Get from current user/auth
    onAddEventClick: () -> Unit = {},
    onEditEventClick: (String) -> Unit = {},
    onDeleteEventClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: OrganizerEventManagementViewModel = hiltViewModel()
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) } // Double confirm

    val uiState by viewModel.uiState.collectAsState()

    // Initialize ViewModel with organizer ID
    LaunchedEffect(Unit) {
        viewModel.loadBannerEvents()
        viewModel.loadMyEvents()
    }

    // Update ViewModel search query
    LaunchedEffect(searchQuery) {
        viewModel.updateSearchQuery(searchQuery)
    }

    val filteredEvents = viewModel.getFilteredEvents()
    
    // If no events and loading, show loading state
    if (uiState.isLoading && filteredEvents.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading your events...", color = Color.Gray)
        }
        return
    }
    
    // Handle delete with double confirmation
    val handleDelete = { eventId: String ->
        if (showDeleteConfirmDialog == null) {
            // First confirmation
            showDeleteConfirmDialog = eventId
        } else if (showDeleteConfirmDialog == eventId) {
            // Second confirmation - actually delete
            viewModel.deleteEvent(eventId) { success, error ->
                showDeleteConfirmDialog = null
                if (success) {
                    // Success handled in ViewModel (refreshes list)
                } else {
                    // Error shown in UI state
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // TARUMT Logo placeholder
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
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
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
                // CHANGE: Home button - weight to push items to sides
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 12.sp) },
                    selected = true,
                    onClick = onHomeClick
                )

                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = Color.DarkGray,
                                    shape = RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Event",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    label = { }, // Empty label for center button
                    selected = false,
                    onClick = onAddEventClick
                )

                // CHANGE: Profile/Me button
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Me", fontSize = 12.sp) },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // Event Banner Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(uiState.bannerEvents.take(3)) { event ->
                        OrganizerEventBanner(event = event)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // My Event Section Header
            item {
                Text(
                    text = "My Event",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Event List
            items(filteredEvents) { event ->
                OrganizerEventCard(
                    event = event,
                    onEditClick = { onEditEventClick(event.id) },
                    onDeleteClick = { handleDelete(event.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Error message display
            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage ?: "Error",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Double Confirm Delete Dialog
        showDeleteConfirmDialog?.let { eventId ->
            val eventName = filteredEvents.find { it.id == eventId }?.name ?: "this event"
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Delete Event") },
                text = { 
                    Text("Are you sure you want to delete \"$eventName\"? This action cannot be undone. Click Delete again to confirm.") 
                },
                confirmButton = {
                    TextButton(
                        onClick = { handleDelete(eventId) }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun OrganizerEventBanner(
    event: EventDisplayModel
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        val context = LocalContext.current

        val imageRes = remember(event.bannerUrl) {
            if (!event.bannerUrl.isNullOrBlank()) {
                context.resources.getIdentifier(
                    event.bannerUrl,
                    "drawable",
                    context.packageName
                )
            } else 0
        }
        Box(
            modifier = Modifier.fillMaxSize(),
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
//                    )
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            // TODO: Load actual image from bannerUrl
//            if (event.bannerUrl == null) {
//                Text(
//                    text = event.name,
//                    color = Color.White,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(16.dp)
//                )
//            } else {
//                // TODO: Use AsyncImage from Coil library
//                // AsyncImage(
//                //     model = event.bannerUrl,
//                //     contentDescription = event.name,
//                //     modifier = Modifier.fillMaxSize(),
//                //     contentScale = ContentScale.Crop
//                // )
//
//            }
//        }
    }
}

@Composable
private fun OrganizerEventCard(
    event: EventDisplayModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Event Banner Image
            Card(
                modifier = Modifier.height(120.dp).width(190.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                val context = LocalContext.current

                val imageRes = remember(event.bannerUrl) {
                    if (!event.bannerUrl.isNullOrBlank()) {
                        context.resources.getIdentifier(
                            event.bannerUrl,
                            "drawable",
                            context.packageName
                        )
                    } else 0
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = event.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp),
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Event Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Event Name
                Text(
                    text = event.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Date Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.date}, ${event.time}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Venue Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.venue,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // EDIT Button
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.height(32.dp).width(125.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Icon",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EDIT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp)) // spacing between buttons

                    // Delete Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


//// ==================== PREVIEW ====================
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun OrganizerEventManagementScreenPreview() {
//    OrganizerEventManagementScreen(
//        onAddEventClick = {},
//        onEditEventClick = {},
//        onDeleteEventClick = {},
//        onHomeClick = {},
//        onProfileClick = {}
//    )
//}