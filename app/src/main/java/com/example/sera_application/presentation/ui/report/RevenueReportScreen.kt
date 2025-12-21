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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sera_application.R
import com.example.sera_application.domain.model.uimodel.PaymentStatistics
import com.example.sera_application.domain.model.uimodel.TopEarningEventUiModel
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.padding(8.dp).weight(4f),
            ) {
                Text(text = "Total Revenue", fontSize = 16.sp, color = Color(0xFF434343))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = String.format("RM %.2f", revenue), fontSize = 32.sp, color = Color(0xFF434343), fontWeight = Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC5E8E0)),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(text = formatRevenueGrowth(revenueGrowth), fontSize = 12.sp, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), letterSpacing = (-0.5).sp)
                }
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFF2F4F7))
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.money),
                    contentDescription = "Icon",
                    modifier = Modifier.size(64.dp).align(Alignment.Center).clip(CircleShape),
                    alpha = 0.7f
                )
            }
        }
    }
}

@Composable
fun LineChart(revenue: List<FloatEntry>) {
    val revenueLine = lineSpec(
        lineColor = Color(0xFF7C7C7C),
        lineThickness = 1.dp
    )

    val startAxis = startAxis(
        valueFormatter = AxisValueFormatter { value, _ ->
            value.toInt().toString()
        }

    )

    Chart(
        chart = lineChart(
            lines = listOf(revenueLine),
            spacing = 0.dp
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
                        Text(text = "🥇", fontSize = 20.sp)
                    }
                    2 -> {
                        Text(text = "🥈", fontSize = 20.sp)
                    }
                    3 -> {
                        Text(text = "🥉", fontSize = 20.sp)
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
                when {
                    event.imagePath?.startsWith("drawable://") == true  -> {
                        val resName = event.imagePath.removePrefix("drawable://")
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
                            model = event.imagePath,
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.padding(8.dp).size(width = 145.dp, height = 180.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFCFF1E6))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxSize()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Success", fontSize = 10.sp, fontWeight = Medium, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp))
                    }
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = paymentStat.successCount.toString(), fontSize = 36.sp, fontWeight = Bold, color = Color(0xFF0CB37C))
                        Text(text = String.format("%.1f%%", paymentStat.successRate), fontSize = 20.sp, color = Color(0xFF53605C))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.padding(8.dp).size(width = 145.dp, height = 180.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECCE))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxSize()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Pending", fontSize = 10.sp, fontWeight = Medium, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp))
                    }
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = paymentStat.pendingCount.toString(), fontSize = 36.sp, fontWeight = Bold, color = Color(0xFFD69322))
                        Text(text = String.format("%.1f%%", paymentStat.pendingRate), fontSize = 20.sp, color = Color(0xFF53605C))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.padding(8.dp).size(width = 145.dp, height = 180.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCDADA))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxSize()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Failed", fontSize = 10.sp, fontWeight = Medium, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp))
                    }
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = paymentStat.failedCount.toString(), fontSize = 36.sp, fontWeight = Bold, color = Color(0xFFE23A3A))
                        Text(text = String.format("%.1f%%", paymentStat.failedRate), fontSize = 20.sp, color = Color(0xFF53605C))
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueReportScreen(
    viewModel: RevenueReportViewModel = viewModel()
) {
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val revenueGrowth by viewModel.revenueGrowth.collectAsState()
    val revenueData by viewModel.revenueData.collectAsState()
    val topEarningEvents by viewModel.topEarningEvents.collectAsState()
    val paymentStats by viewModel.paymentStats.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Total Revenue",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            TotalRevenueCard(totalRevenue, revenueGrowth)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Revenue Trend",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            FilterButtonRow(
                options = listOf("Weekly", "Monthly"),
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
                LineChart(revenueData)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Top 3 Earning Events",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        items(topEarningEvents) { event ->
            Top3EarningEvents(event)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Payment Status",
                fontSize = 16.sp,
                fontWeight = Bold,
                color = Color.Black,
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