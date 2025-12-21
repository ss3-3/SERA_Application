package com.example.sera_application.presentation.ui.dashboard

import kotlin.collections.take
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sera_application.domain.model.uimodel.EventListUiModel
import com.example.sera_application.domain.model.uimodel.Item
import com.example.sera_application.presentation.ui.report.AllEventList
import com.example.sera_application.presentation.viewmodel.dashboard.OrganizerDashboardViewModel

@Composable
fun OrganizerEventPreviewSection(
    events: List<EventListUiModel>,
    onMoreClick: () -> Unit
) {
    val previewEvents = events.take(2)

    Column {
        previewEvents.forEach { event ->
            AllEventList(event)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (events.size > 2) {
            Text(
                text = "More events...",
                fontSize = 10.sp,
                color = Color(0xFF6B7280),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable { onMoreClick() }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun QuickStatCard(
    totalParticipants: Int,
    averageRevenue: Double
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(400.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF51565B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Total Participants",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalParticipants people",
                        fontSize = 28.sp,
                        fontWeight = SemiBold,
                        color = Color.White
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF51565B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Average Revenue per Event",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("RM %.2f", averageRevenue),
                        fontSize = 28.sp,
                        fontWeight = SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun OrganizerDashboardScreen(
    organizerId: String,
    viewModel: OrganizerDashboardViewModel = viewModel()
) {
    val statsItems by viewModel.statsItems.collectAsState()
    val events by viewModel.eventUiList.collectAsState()
    val totalParticipants by viewModel.totalParticipants.collectAsState()
    val averageRevenue by viewModel.averageRevenue.collectAsState()
    val eventCount = events.size

    LaunchedEffect(Unit) {
        viewModel.loadOrganizerData(organizerId = organizerId)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = "My Performance",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(statsItems.size) { index ->
                    val item = statsItems[index]
                    StatCard(
                        title = item.title,
                        value = item.value,
                        backgroundColor = item.bgColor,
                        textColor = item.textColor
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "My Events ($eventCount)",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            OrganizerEventPreviewSection(
                events = events,
                onMoreClick = {
                    // TODO: navigate to AllOrganizerEventsScreen
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quick Statistics",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            QuickStatCard(totalParticipants, averageRevenue)
        }
    }
}

@Composable
fun AllOrganizerEventsScreen(
    events: List<EventListUiModel>
) {
    val eventCount = events.size

    LazyColumn {
        item {
            Text(
                text = "My Events ($eventCount)",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        items(events) { event ->
            AllEventList(event)
        }
    }
}