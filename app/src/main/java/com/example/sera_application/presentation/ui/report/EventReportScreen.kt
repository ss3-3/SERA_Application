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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7))
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
                    .shadow(elevation = 16.dp)
            ) {
                when {
                    event.picture.startsWith("drawable://") -> {
                        val resName = event.picture.removePrefix("drawable://")
                        val resId = context.resources.getIdentifier(
                            resName,
                            "drawable",
                            context.packageName
                        )
                        Image(
                            painter = painterResource(resId),
                            contentDescription = "event image",
                            modifier = Modifier
                                .size(width = 60.dp, height = 59.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    else -> {
                        AsyncImage(
                            model = event.picture,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 60.dp, height = 59.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                Text(text = event.title, fontSize = 10.sp, color = Color.Black, fontWeight = Bold)
                Text(text = "by ${event.organizer}", fontSize = 8.sp, color = Color(0xFF868C97), fontWeight = FontWeight.SemiBold, style = TextStyle(lineHeight = 30.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = event.description, fontSize = 8.sp, color = Color(0xFFB1B1B1), fontWeight = FontWeight.Medium, style = TextStyle(lineHeight = 10.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.money),
                        contentDescription = "Revenue",
                        modifier = Modifier.size(16.dp).align(Alignment.Center)
                    )
                }
                Text(text = String.format("%.2f", event.revenue), fontSize = 9.sp, fontWeight = Bold, letterSpacing = (-0.01).sp)
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Participants",
                        modifier = Modifier.size(18.dp).align(Alignment.Center)
                    )
                }
                Text(text = "${event.participants}", fontSize = 9.sp, fontWeight = Bold, letterSpacing = (-0.01).sp)
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

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = formatDate(startDate),
            onValueChange = { },
            label = { Text("Start Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showStartPicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select start date")
                }
            },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = formatDate(endDate),
            onValueChange = { },
            label = { Text("End Date") },
            readOnly = true,
            enabled = startDate != null,
            trailingIcon = {
                IconButton(
                    onClick = { showEndPicker = true },
                    enabled = startDate != null
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select end date")
                }
            },
            modifier = Modifier.weight(1f)
        )
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

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DateFilterSection(
                onDateRangeSelected = { startDate, endDate ->
                    viewModel.filterByDateRange(startDate, endDate)
                }
            )
        }

        item {
            Text(
                text = "Event ($eventCount)",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        items(events) { event ->
            AllEventList(event = event)
        }
    }
}