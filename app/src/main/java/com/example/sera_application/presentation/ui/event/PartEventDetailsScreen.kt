package com.example.sera_application.presentation.ui.event

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.sera_application.presentation.viewmodel.event.EventDetailsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EventDetailRow(
    icon: ImageVector,
    value: String,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    onBackClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {},
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val eventDomain = uiState.event
    val event = eventDomain?.let { EventDisplayModel.fromDomain(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.92f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // !!!Book Now Button - Now fixed at bottom!!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Available Seats Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventSeat,
                        contentDescription = "Seats",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Available Seats: ${event?.availableSeats ?: 0}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Book Now Button
                Button(
                    onClick = onBookNowClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42A5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Book Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

    ) { padding ->
        if (event == null) {
            // Simple empty state while loading or if not found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Loading event...",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(padding)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Event Banner and Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Event Banner Image (smaller, rounded)
                            Card(
                                modifier = Modifier.size(140.dp),
                                shape = RoundedCornerShape(16.dp),
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
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Event Info Column (beside banner)
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Spacer(modifier = Modifier.height(15.dp))

                                // Event Name
                                Text(
                                    text = event.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Organizer Name
                                Text(
                                    text = event.organizer,
                                    fontSize = 15.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(45.dp))

                                // Price Range
                                Text(
                                    text = event.priceRange,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E63)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Date
                        EventDetailRow(
                            icon = Icons.Default.CalendarToday,
                            value = event.date,
                            iconTint = Color(0xFFFFA726)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Time
                        EventDetailRow(
                            icon = Icons.Default.AccessTime,
                            value = event.time.ifBlank { "Not specified" },
                            iconTint = Color(0xFFFFA726)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Venue
                        EventDetailRow(
                            icon = Icons.Default.LocationOn,
                            value = event.venue.ifBlank { "Not specified" },
                            iconTint = Color(0xFFFFA726)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Description Section
                        Text(
                            text = "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = event.description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun EventDetailRow(
//    icon: ImageVector,
//    value: String,
//    iconTint: Color
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = iconTint,
//            modifier = Modifier.size(20.dp)
//        )
//        Spacer(modifier = Modifier.width(10.dp))
//        Text(
//            text = value,
//            fontSize = 14.sp,
//            color = Color.Black,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}

// ==================== PREVIEW ====================

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun EventDetailsScreenPreview() {
//    val sampleEvent = EventDisplayModel(
//        id = "1",
//        name = "MUSIC FIESTA 6.0",
//        organizer = "Music Society TARUMT",
//        date = "12/01/2026",
//        time = "19:00 PM - 22:00 PM",
//        venue = "Rimba, TARUMT",
//        priceRange = "RM 35 - RM 60",
//        availableSeats = 180,
//        totalSeats = 200,
//        category = EventCategoryUI.MUSIC,
//        description = "Music Fiesta 6.0 is a large-scale campus concert and carnival proudly organized by Music Society of Tunku Abdul Rahman University of Management and Technology (TARUMT).",
//        bannerUrl = null
//    )
//
//    // Preview without ViewModel dependency
//    EventDetailsContentPreview(sampleEvent)
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun EventDetailsContentPreview(event: EventDisplayModel) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Event Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
//                navigationIcon = {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color(0xFF1A1A1A).copy(alpha = 0.92f),
//                    titleContentColor = Color.White,
//                    navigationIconContentColor = Color.White
//                )
//            )
//        },
//        bottomBar = {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(horizontal = 20.dp, vertical = 16.dp)
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(
//                            color = Color(0xFFFFF8E1),
//                            shape = RoundedCornerShape(12.dp)
//                        )
//                        .padding(14.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.EventSeat,
//                        contentDescription = "Seats",
//                        tint = Color(0xFFFFA726),
//                        modifier = Modifier.size(22.dp)
//                    )
//                    Spacer(modifier = Modifier.width(10.dp))
//                    Text(
//                        text = "Available Seats: ${event.availableSeats}",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color(0xFFE65100)
//                    )
//                }
//                Spacer(modifier = Modifier.height(12.dp))
//                Button(
//                    onClick = {},
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(52.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF42A5F5)
//                    ),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Text(
//                        text = "Book Now",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White
//                    )
//                }
//            }
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFFF5F5F5))
//                .padding(padding)
//        ) {
//            item {
//                Column(
//                    modifier = Modifier.padding(20.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Card(
//                            modifier = Modifier.size(140.dp),
//                            shape = RoundedCornerShape(16.dp),
//                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .background(
//                                        brush = Brush.verticalGradient(
//                                            colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
//                                        )
//                                    ),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = event.name,
//                                    color = Color.White,
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    textAlign = TextAlign.Center,
//                                    modifier = Modifier.padding(8.dp)
//                                )
//                            }
//                        }
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Column(
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Spacer(modifier = Modifier.height(15.dp))
//                            Text(
//                                text = event.name,
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(6.dp))
//                            Text(
//                                text = event.organizer,
//                                fontSize = 15.sp,
//                                color = Color.Gray
//                            )
//                            Spacer(modifier = Modifier.height(45.dp))
//                            Text(
//                                text = event.priceRange,
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFFE91E63)
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(20.dp))
//                    EventDetailRow(
//                        icon = Icons.Default.CalendarToday,
//                        value = event.date,
//                        iconTint = Color(0xFFFFA726)
//                    )
//                    Spacer(modifier = Modifier.height(14.dp))
//                    EventDetailRow(
//                        icon = Icons.Default.AccessTime,
//                        value = event.time,
//                        iconTint = Color(0xFFFFA726)
//                    )
//                    Spacer(modifier = Modifier.height(14.dp))
//                    EventDetailRow(
//                        icon = Icons.Default.LocationOn,
//                        value = event.venue,
//                        iconTint = Color(0xFFFFA726)
//                    )
//                    Spacer(modifier = Modifier.height(24.dp))
//                    Divider(
//                        color = Color(0xFFE0E0E0),
//                        thickness = 1.dp
//                    )
//                    Spacer(modifier = Modifier.height(20.dp))
//                    Text(
//                        text = "Description",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Text(
//                        text = event.description,
//                        fontSize = 14.sp,
//                        color = Color(0xFF666666),
//                        lineHeight = 22.sp,
//                        textAlign = TextAlign.Justify
//                    )
//                }
//            }
//        }
//    }
//}}