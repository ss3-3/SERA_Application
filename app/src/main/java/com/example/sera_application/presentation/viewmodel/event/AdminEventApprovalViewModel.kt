package com.example.sera_application.presentation.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.sera_application.presentation.viewmodel.event.AdminEventApprovalViewModel
import com.example.sera_application.utils.DateTimeFormatterUtil
import com.example.sera_application.presentation.ui.components.SafeImageLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventApprovalScreen(
    eventId: String,
    onBackClick: () -> Unit = {},
    onApproveClick: () -> Unit = {},
    onRejectClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    viewModel: AdminEventApprovalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.event
    val context = LocalContext.current

    // Load event details when screen opens
    LaunchedEffect(eventId) {
        if (eventId.isNotEmpty()) {
            viewModel.loadEventDetails(eventId)
        }
    }

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    
    // Handle approval/rejection success - show feedback and navigate back
    LaunchedEffect(uiState.isApprovalSuccess) {
        if (uiState.isApprovalSuccess) {
            android.widget.Toast.makeText(context, "Event approved successfully!", android.widget.Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(500) // Small delay for user feedback
            onBackClick() // Navigate back after success
        }
    }
    
    LaunchedEffect(uiState.isRejectionSuccess) {
        if (uiState.isRejectionSuccess) {
            android.widget.Toast.makeText(context, "Event rejected successfully!", android.widget.Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(500) // Small delay for user feedback
            onBackClick() // Navigate back after success
        }
    }
    
    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = event?.name ?: "Event Details",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .background(
                                color = Color(0xFF424242),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Approve and Reject Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Approve Button
                    Button(
                        onClick = { showApproveDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A9FEE)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Approve",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Reject Button
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4A9FEE)
                        ),
                        border = BorderStroke(2.dp, Color(0xFF4A9FEE))
                    ) {
                        Text(
                            text = "Reject",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel Button
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8E8E8)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6F6F6F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { padding ->
        if (event == null) {
            // Loading or error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoading) {
                        Text("Loading event details...", color = Color.White)
                    } else {
                        Text(
                            text = uiState.errorMessage ?: "Event not found",
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .padding(padding)
            ) {
                // Event Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1A1A2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        SafeImageLoader(
                            imagePath = event.bannerUrl,
                            contentDescription = event.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Event Details Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxSize(),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Event Name
                            Text(
                                text = event.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Description Section
                            Text(
                                text = "Description",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = event.description,
                                fontSize = 14.sp,
                                color = Color.Black,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color(0xFFE0E0E0))

                            Spacer(modifier = Modifier.height(20.dp))

                            // Seats Information Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total rock zone seats",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.rockZoneSeats,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total normal zone seats",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.normalZoneSeats,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color(0xFFE0E0E0))

                            Spacer(modifier = Modifier.height(20.dp))

                            // Date and Duration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Event date",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateTimeFormatterUtil.formatDate(event.date),
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(
//                                        text = "Event duration",
//                                        fontSize = 13.sp,
//                                        color = Color.Gray,
//                                        fontWeight = FontWeight.Medium
//                                    )
//                                    Spacer(modifier = Modifier.height(4.dp))
//                                    Text(
//                                        text = event.duration,
//                                        fontSize = 16.sp,
//                                        color = Color.Black,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color(0xFFE0E0E0))

                            Spacer(modifier = Modifier.height(20.dp))

                            // Time Information
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Event start time",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateTimeFormatterUtil.formatTime(event.startTime),
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Event end time",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateTimeFormatterUtil.formatTime(event.endTime),
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(color = Color(0xFFE0E0E0))

                            Spacer(modifier = Modifier.height(20.dp))

                            // Venue and Organizer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Event venue",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.venue,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Event organizer",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.organizer,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

//            // Bottom spacing
//            item {
//                Spacer(modifier = Modifier.height(16.dp))
//            }
            }
        }

        // Approve Confirmation Dialog
        if (showApproveDialog && event != null) {
            AlertDialog(
                onDismissRequest = { showApproveDialog = false },
                title = { Text("Approve Event") },
                text = { Text("Are you sure you want to approve this event?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showApproveDialog = false
                            viewModel.approveEvent(event.id) { success, error ->
                                // Success/error handling is done in LaunchedEffect
                            }
                        },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF4CAF50)
                            )
                        } else {
                            Text("Approve", color = Color(0xFF4CAF50))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApproveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Reject Confirmation Dialog
        if (showRejectDialog && event != null) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Reject Event") },
                text = { Text("Are you sure you want to reject this event?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRejectDialog = false
                            viewModel.rejectEvent(event.id) { success, error ->
                                // Success/error handling is done in LaunchedEffect
                            }
                        },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Red
                            )
                        } else {
                            Text("Reject", color = Color.Red)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }}

//// ==================== PREVIEW ====================
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun AdminEventApprovalScreenPreview() {
//    AdminEventApprovalScreen(eventId = "1")
//}