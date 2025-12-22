package com.example.sera_application.presentation.ui.report

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sera_application.R
import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import com.example.sera_application.domain.model.uimodel.TopEarningEventUiModel
import com.example.sera_application.domain.usecase.report.ReportConstants
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.patrykandpatryk.vico.core.entry.entryModelOf
import com.example.sera_application.presentation.viewmodel.report.RevenueReportViewModel

@SuppressLint("DefaultLocale")
fun formatRevenueGrowth(percentage: Double): String {
    return if (percentage >= 0) {
        String.format("+%.1f%%", percentage)
    } else {
        String.format("%.1f%%", percentage)
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TotalRevenueCard(
    revenue: Double,
    revenueGrowth: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF10B981).copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.1f),
                            Color(0xFF059669).copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Revenue",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("RM %.2f", revenue),
                        fontSize = 36.sp,
                        color = Color(0xFF1E293B),
                        fontWeight = Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (revenueGrowth >= 0) 
                                Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = formatRevenueGrowth(revenueGrowth),
                            fontSize = 13.sp,
                            color = if (revenueGrowth >= 0) Color(0xFF059669) else Color(0xFFDC2626),
                            fontWeight = SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.money),
                        contentDescription = "Revenue Icon",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(revenue: List<FloatEntry>) {
    android.util.Log.d("LineChart", "Rendering chart with ${revenue.size} data points")
    
    if (revenue.isEmpty()) {
        android.util.Log.w("LineChart", "No data to display in chart")
        return
    }
    
    val revenueLine = lineSpec(
        lineColor = Color(0xFF7C7C7C),
        lineThickness = 2.dp
    )

    val startAxis = startAxis(
        valueFormatter = AxisValueFormatter { value, _ ->
            String.format("%.0f", value)
        }
    )

    Chart(
        chart = lineChart(
            lines = listOf(revenueLine),
            spacing = 8.dp
        ),
        model = entryModelOf(revenue),
        startAxis = startAxis,
        bottomAxis = bottomAxis()
    )
}

@Composable
fun Top3EarningEvents(
    event: TopEarningEventUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
        shape = RoundedCornerShape(28.dp)
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).padding(8.dp)
            ) {
                when (event.rank) {
                    1 -> {
                        Text(text = ReportConstants.RANK_1_EMOJI, fontSize = 20.sp)
                    }
                    2 -> {
                        Text(text = ReportConstants.RANK_2_EMOJI, fontSize = 20.sp)
                    }
                    3 -> {
                        Text(text = ReportConstants.RANK_3_EMOJI, fontSize = 20.sp)
                    }
                    else -> {
                        Text(text = event.rank.toString(), fontSize = 20.sp, fontWeight = Bold)
                    }
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(44.dp)
            ) {
                com.example.sera_application.presentation.ui.components.SafeImageLoader(
                    imagePath = event.imagePath,
                    contentDescription = "Event Image",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.fillMaxHeight().padding(8.dp)
            ) {
                Text(text = event.name, fontSize = 15.sp, fontWeight = SemiBold, color = Color(0xFF616263))
                Text(text = String.format("RM %.2f", event.revenue), fontSize = 10.sp, fontWeight = Normal, color = Color(0xFF616263))
            }
        }
    }
}

@Composable
fun PaymentStatusCard(
    paymentStat: PaymentStatistics
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .size(width = 160.dp, height = 160.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF10B981).copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Success",
                            fontSize = 11.sp,
                            fontWeight = SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = paymentStat.successCount.toString(),
                            fontSize = 40.sp,
                            fontWeight = Bold,
                            color = Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f%%", paymentStat.successRate),
                            fontSize = 16.sp,
                            color = Color(0xFF64748B),
                            fontWeight = Medium
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .size(width = 160.dp, height = 160.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Pending",
                            fontSize = 11.sp,
                            fontWeight = SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = paymentStat.pendingCount.toString(),
                            fontSize = 40.sp,
                            fontWeight = Bold,
                            color = Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f%%", paymentStat.pendingRate),
                            fontSize = 16.sp,
                            color = Color(0xFF64748B),
                            fontWeight = Medium
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .size(width = 160.dp, height = 160.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFFEF4444).copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Failed",
                            fontSize = 11.sp,
                            fontWeight = SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = paymentStat.failedCount.toString(),
                            fontSize = 40.sp,
                            fontWeight = Bold,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%.1f%%", paymentStat.failedRate),
                            fontSize = 16.sp,
                            color = Color(0xFF64748B),
                            fontWeight = Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueReportScreen(
    viewModel: RevenueReportViewModel = hiltViewModel()
) {
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val revenueGrowth by viewModel.revenueGrowth.collectAsState()
    val revenueData by viewModel.revenueData.collectAsState()
    val topEarningEvents by viewModel.topEarningEvents.collectAsState()
    val paymentStats by viewModel.paymentStats.collectAsState()

    // Ensure data is loaded when screen is displayed
    LaunchedEffect(Unit) {
        try {
            viewModel.loadRevenueData()
        } catch (e: Exception) {
            android.util.Log.e("RevenueReportScreen", "Error loading revenue data: ${e.message}", e)
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TotalRevenueCard(totalRevenue, revenueGrowth)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revenue Trend",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            FilterButtonRow(
                options = listOf(ReportConstants.PERIOD_WEEKLY, ReportConstants.PERIOD_MONTHLY),
                onOptionSelected = { period ->
                    viewModel.loadTrendData(period)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(width = 1.dp, color = Color(0xFF4E4E4E))
            ) {
                if (revenueData.isEmpty()) {
                    // Show message when no data
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No revenue data available",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                } else {
                    LineChart(revenueData)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Top ${ReportConstants.TOP_EVENTS_LIMIT} Earning Events",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        items(topEarningEvents) { event ->
            Top3EarningEvents(event)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Payment Status",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            paymentStats?.let { stats ->
                PaymentStatusCard(stats)
            }
        }
    }
}