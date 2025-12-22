package com.example.sera_application.presentation.ui.report

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sera_application.R
import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.presentation.viewmodel.report.EventReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale", "LocalContextResourcesRead")
@Composable
fun AllEventList(
    event: EventListUiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced Event Image using SafeImageLoader
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            ) {
                com.example.sera_application.presentation.ui.components.SafeImageLoader(
                    imagePath = event.picture,
                    contentDescription = "event image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.size(12.dp))
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "by ${event.organizer}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = event.description,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.size(8.dp))
            
            // Enhanced Stats Section
            Column(
                modifier = Modifier.padding(start = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Revenue Card
                Card(
                    modifier = Modifier.size(56.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.money),
                            contentDescription = "Revenue",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format("RM%.0f", event.revenue),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
                
                // Participants Card
                Card(
                    modifier = Modifier.size(56.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Participants",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF2563EB)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${event.participants}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterSection(
    onDateRangeSelected: (Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    fun formatDate(millis: Long?): String {
        return millis?.let {
            val date = Date(it)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        } ?: "Please select"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = formatDate(startDate),
                onValueChange = { },
                label = { Text("Start Date", fontSize = 13.sp) },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showStartPicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select start date",
                            tint = Color(0xFF6366F1)
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = formatDate(endDate),
                onValueChange = { },
                label = { Text("End Date", fontSize = 13.sp) },
                readOnly = true,
                enabled = startDate != null,
                trailingIcon = {
                    IconButton(
                        onClick = { showEndPicker = true },
                        enabled = startDate != null
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select end date",
                            tint = if (startDate != null) Color(0xFF6366F1) else Color(0xFFCBD5E1)
                        )
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

    if (showStartPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDate = datePickerState.selectedDateMillis
                        if (endDate != null && startDate != null && startDate!! > endDate!!) {
                            endDate = null
                        }
                        showStartPicker = false
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate,
            initialDisplayedMonthMillis = startDate
        )

        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis ?: startDate
                        if (selectedDate != null && selectedDate >= startDate!!) {
                            endDate = selectedDate
                            showEndPicker = false
                        }
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(startDate, endDate) {
        if (startDate != null && endDate != null) {
            onDateRangeSelected(startDate, endDate)
        }
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
fun EventReportScreen(
    viewModel: EventReportViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    val eventCount by viewModel.eventCount.collectAsState()

    // Ensure data is loaded when screen is displayed
    LaunchedEffect(Unit) {
        try {
            viewModel.loadAllEvents()
        } catch (e: Exception) {
            android.util.Log.e("EventReportScreen", "Error loading events: ${e.message}", e)
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            DateFilterSection(
                onDateRangeSelected = { startDate, endDate ->
                    viewModel.filterByDateRange(startDate, endDate)
                }
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Events",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$eventCount",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.2f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF6366F1)
                        )
                    }
                }
            }
        }

        if (events.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "No events found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Try adjusting your date range",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(events) { event ->
                AllEventList(event = event)
            }
        }
    }
}