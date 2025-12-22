package com.example.sera_application.presentation.ui.report

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sera_application.domain.model.uimodel.TopParticipantUiModel
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.patrykandpatryk.vico.core.entry.entryModelOf
import kotlin.text.removePrefix
import kotlin.text.startsWith
import com.example.sera_application.presentation.viewmodel.report.*
import com.google.common.math.LinearTransformation.horizontal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDropdownSelector() {
    val months = listOf(
        "January 2025", "February 2025", "March 2025",
        "April 2025", "May 2025", "June 2025",
        "July 2025", "August 2025", "September 2025",
        "October 2025", "November 2025", "December 2025"
    )
    var selectedMonth by remember { mutableStateOf(months[9]) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = selectedMonth,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Month") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        selectedMonth = month
                        expanded = false
                    }
                )
            }
        }
    }
}

fun calculateNewUserGrowth(currentMonthNewUsers: Int, lastMonthNewUsers: Int): Float  {
    return when {
        lastMonthNewUsers == 0 && currentMonthNewUsers > 0 -> 100f
        lastMonthNewUsers == 0 && currentMonthNewUsers == 0 -> 0f
        else -> ((currentMonthNewUsers - lastMonthNewUsers).toFloat() / lastMonthNewUsers) * 100
    }
}

fun calculateParticipantGrowth(currentMonthParticipants: Int, totalParticipants: Int): Float  {
    return if (totalParticipants > 0) {
        (currentMonthParticipants.toFloat() / totalParticipants) * 100
    } else {
        0f
    }
}

@SuppressLint("DefaultLocale")
fun formatGrowth(percentage: Float): String {
    return if (percentage >= 0) {
        String.format("+%.0f%%", percentage)
    } else {
        String.format("%.0f%%", percentage)
    }
}

@Composable
fun UserStatCard(
    totalUser: Int,
    newUser: Int,
    participants: Int,
    newUserGrowth: Float,
    participantGrowth: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Total Users Card with gradient
        Card(
            modifier = Modifier
                .height(120.dp)
                .weight(1f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFF6366F1).copy(alpha = 0.3f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Users",
                        fontSize = 13.sp,
                        fontWeight = SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = totalUser.toString(),
                        fontSize = 32.sp,
                        fontWeight = Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // New Users Card
        Card(
            modifier = Modifier
                .height(120.dp)
                .weight(1f)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "New Users",
                    fontSize = 13.sp,
                    fontWeight = SemiBold,
                    color = Color(0xFF64748B)
                )
                Column {
                    Text(
                        text = newUser.toString(),
                        fontSize = 32.sp,
                        fontWeight = Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = formatGrowth(newUserGrowth),
                        fontSize = 11.sp,
                        fontWeight = Normal,
                        color = if (newUserGrowth >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }
        }
        
        // Participants Card
        Card(
            modifier = Modifier
                .height(120.dp)
                .weight(1f)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Participants",
                    fontSize = 13.sp,
                    fontWeight = SemiBold,
                    color = Color(0xFF64748B)
                )
                Column {
                    Text(
                        text = participants.toString(),
                        fontSize = 32.sp,
                        fontWeight = Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = String.format("%.0f%%", participantGrowth),
                        fontSize = 11.sp,
                        fontWeight = Normal,
                        color = Color(0xFF6366F1)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterButtonRow(
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(0) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = index == selectedIndex

            OutlinedButton(
                onClick = {
                    selectedIndex = index
                    onOptionSelected(text)
                },
                shape = RoundedCornerShape(20.dp),
                border = if (isSelected) null
                else BorderStroke(1.dp, Color.Black),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color(0xFF4E4E4E) else Color.White,
                    contentColor = if (isSelected) Color.White else Color(0xFF4E4E4E)
                )
            ) {
                Text(
                    text = text,
                    fontSize = 10.sp,
                    fontWeight = Bold
                )
            }
        }
    }
}

@Composable
fun MultiLineChart(totalUsers: List<FloatEntry>, newUsers: List<FloatEntry>) {
    val totalUsersLine = lineSpec(
        lineColor = Color(0xFFE9573F),
        lineThickness = 2.dp
    )

    val newUsersLine = lineSpec(
        lineColor = Color(0xFF3FB0DB),
        lineThickness = 2.dp
    )

    val startAxis = startAxis(
        valueFormatter = AxisValueFormatter { value, _ ->
            value.toInt().toString()
        }

    )

    Chart(
        chart = lineChart(
            lines = listOf(totalUsersLine, newUsersLine),
            spacing = 0.dp
        ),
        model = entryModelOf(totalUsers, newUsers),
        startAxis = startAxis,
        bottomAxis = bottomAxis()
    )
}

@Composable
fun TopParticipantsList(
    participants: List<TopParticipantUiModel>
) {
    Row {
        participants.forEach {
            TopParticipantItem(it)
        }
    }
}

@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun TopParticipantItem(
    participant: TopParticipantUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
        shape = RoundedCornerShape(28.dp)
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).padding(8.dp)
            ) {
                when (participant.rank) {
                    1 -> {
                        Text(text = "🥇", fontSize = 20.sp)
                    }
                    2 -> {
                        Text(text = "🥈", fontSize = 20.sp)
                    }
                    3 -> {
                        Text(text = "🥉", fontSize = 20.sp)
                    }
                    else -> {
                        Text(text = participant.rank.toString(), fontSize = 20.sp, fontWeight = Bold)
                    }
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(44.dp)
            ) {
                when {
                    participant.profileImagePath?.startsWith("drawable://") == true  -> {
                        val resName = participant.profileImagePath.removePrefix("drawable://")
                        val resId = context.resources.getIdentifier(
                            resName,
                            "drawable",
                            context.packageName
                        )
                        Image(
                            painter = painterResource(resId),
                            contentDescription = "event image",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    else -> {
                        AsyncImage(
                            model = participant.profileImagePath,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxHeight().padding(8.dp)
            ) {
                Text(text = participant.name, fontSize = 15.sp, fontWeight = SemiBold, color = Color(0xFF616263))
                Text(text = "${participant.participationCount} events joined", fontSize = 10.sp, fontWeight = Normal, color = Color(0xFF797A7B))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun EventParticipationCard(
    user: Int,
    userPercentage: Double,
    averageEvent: Double,
    totalBooking: Int
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
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Participating User",
                        fontSize = 16.sp,
                        fontWeight = SemiBold,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StaticLoadingBar(userPercentage, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = String.format("%d users (%.0f%%)", user, (userPercentage * 100)),
                            fontSize = 12.sp,
                            fontWeight = SemiBold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF51565B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Average Events per User",
                        fontSize = 16.sp,
                        fontWeight = SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = String.format("%.1f", averageEvent),
                        fontSize = 12.sp,
                        fontWeight = SemiBold,
                        color = Color.White
                    )
                }
            }
            Card(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF51565B)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Total Bookings",
                        fontSize = 16.sp,
                        fontWeight = SemiBold,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StaticLoadingBar(100.0, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "$totalBooking",
                            fontSize = 12.sp,
                            fontWeight = SemiBold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaticLoadingBar(
    progress: Double,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    backgroundColor: Color = Color(0xFF474747),
    progressColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0.0, 1.0).toFloat())
                .clip(RoundedCornerShape(50))
                .background(progressColor)
        )
    }
}

@Composable
fun UserReportScreen(
    viewModel: UserReportViewModel = hiltViewModel()
) {
    val totalUsers by viewModel.totalUsers.collectAsState()
    val newUsers by viewModel.newUsers.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val lastMonthNewUsers by viewModel.lastMonthNewUsers.collectAsState()
    val lastMonthParticipants by viewModel.lastMonthParticipants.collectAsState()
    val totalUsersData by viewModel.totalUsersData.collectAsState()
    val newUsersData by viewModel.newUsersData.collectAsState()
    val topParticipants by viewModel.topParticipants.collectAsState()
    val participatingUsers by viewModel.participatingUsers.collectAsState()
    val userPercentage by viewModel.userPercentage.collectAsState()
    val averageEventsPerUser by viewModel.averageEventsPerUser.collectAsState()
    val totalBookings by viewModel.totalBookings.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserData() // Load data when screen is first displayed
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "User Statistics",
                        fontSize = 18.sp,
                        fontWeight = Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    MonthDropdownSelector()
                }
            }
        }

        item {
            UserStatCard(
                totalUsers,
                newUsers,
                participants,
                // error
                newUserGrowth = calculateNewUserGrowth(newUsers, lastMonthNewUsers),
                participantGrowth = calculateParticipantGrowth(participants, totalUsers)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 8.dp),

                    thickness = 1.dp,
                    color = Color(0xFF9A9999)
                )
            }
        }

        item {
            Text(
                text = "User Growth Trend",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            FilterButtonRow(
                options = listOf("7 days", "30 days", "3 months"),
                onOptionSelected = { periodText ->
                    val days = when(periodText) {
                        "7 days" -> 7
                        "30 days" -> 30
                        "3 months" -> 90
                        else -> 7
                    }
                    viewModel.loadTrendData(days)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(width = 1.dp, color = Color(0xFF4E4E4E))
            ) {
                MultiLineChart(totalUsersData, newUsersData)
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 16.dp),

                    thickness = 1.dp,
                    color = Color(0xFF9A9999)
                )
            }
        }

        item {
            Text(
                text = "Top Participants",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = "Most active users this month",
                fontSize = 12.sp,
                fontWeight = Normal,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        items(topParticipants) { participant ->
            TopParticipantItem(participant)
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 16.dp),

                    thickness = 1.dp,
                    color = Color(0xFF9A9999)
                )
            }
        }

        item {
            Text(
                text = "Event Participation",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            EventParticipationCard(participatingUsers, userPercentage, averageEventsPerUser, totalBookings)
        }
    }
}