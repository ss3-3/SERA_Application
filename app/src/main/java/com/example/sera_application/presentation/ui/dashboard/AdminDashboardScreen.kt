package com.example.sera_application.presentation.ui.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sera_application.R
import com.example.sera_application.domain.model.uimodel.Item
import com.example.sera_application.presentation.viewmodel.dashboard.AdminDashboardViewModel
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.patrykandpatryk.vico.core.entry.entryModelOf

@Composable
fun StatCard(
    title: String,
    value: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(140.dp)
            .height(100.dp)
    ) {
        // the outside card with gray stroke
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFE5E7EB)
            )
        ) {}

        // the front card with data inside
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 12.dp, y = 12.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 26.sp,
                    fontWeight = Bold,
                    color = textColor
                )
            }
        }
    }
}

@SuppressLint("FrequentlyChangingValue")
@Composable
fun StatsSliderSection(statList: List<Item>) {
    val listState = rememberLazyListState()
    var selectedIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        selectedIndex = listState.firstVisibleItemIndex
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(statList.size) { index ->
                val item = statList[index]
                StatCard(
                    title = item.title,
                    value = item.value,
                    backgroundColor = item.bgColor,
                    textColor = item.textColor
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(statList.size) { index ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.Black
                                else Color.LightGray.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MultiLineChartVico(bookings: List<FloatEntry>, users: List<FloatEntry>) {
    val bookingLine = lineSpec(
        lineColor = Color(0xFF3B82F6),
        lineThickness = 3.dp
    )

    val userLine = lineSpec(
        lineColor = Color(0xFF10B981),
        lineThickness = 3.dp
    )

    val startAxis = startAxis(
        valueFormatter = AxisValueFormatter { value, _ ->
            value.toInt().toString()
        }

    )

    Chart(
        chart = lineChart(
            lines = listOf(bookingLine, userLine),
            spacing = 0.dp
        ),
        model = entryModelOf(bookings, users),
        startAxis = startAxis,
        bottomAxis = bottomAxis()
    )
}

@Composable
fun TrendItem(
    value: String,
    label: String,
    dataColor: Color,
    titleColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = dataColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = titleColor
        )
    }
}

@Composable
fun TrendChartSection(
    bookings: List<FloatEntry>,
    users: List<FloatEntry>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .height(200.dp)
                    .border(width = 1.dp, color = Color.Black)
            ) {
                MultiLineChartVico(
                    bookings = bookings,
                    users = users
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(0.3f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrendItem("+15.3%", "User Growth", Color(0xFF10B981), Color(0xFFB5B5B5))
                TrendItem("+12.8%", "Bookings", Color(0xFF10B981), Color(0xFFB5B5B5))
                TrendItem("+18.5%", "Revenue", Color(0xFF10B981), Color(0xFFB5B5B5))
            }
        }
    }
}

@Composable
fun PopularEvent(
    title: String,
    participants: Int,
    picture: Int,
    rank: Int,
    rankColor: Color,
    rankNumberColor: Color
) {
    val imageSize = when (rank) {
        1 -> 50.dp
        2, 3 -> 40.dp
        else -> 40.dp
    }

    val badgeSize = when (rank) {
        1 -> 18.dp
        2, 3 -> 16.dp
        else -> 0.dp
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = picture),
                    contentDescription = "event image",
                    modifier = Modifier
                        .size(imageSize)
                        .clip(CircleShape)
                        .border(width = 1.dp, color = Color.LightGray, shape = CircleShape),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .size(badgeSize)
                        .clip(CircleShape)
                        .background(rankColor)
                        .align(Alignment.TopEnd)
                        .offset(
                            x = (badgeSize.value * 0.3).dp,
                            y = (-badgeSize.value * 0.2).dp
                        )
                ) {
                    Text(
                        text = rank.toString(),
                        color = rankNumberColor,
                        fontSize = 12.sp,
                        fontWeight = Bold
                    )
                }
            }
            Text(text = title, fontSize = 12.sp, color = Color.Black, fontWeight = Bold)
            Text(
                text = "$participants people",
                fontSize = 7.sp,
                color = Color(0xFF909090),
                style = TextStyle(lineHeight = 30.sp)
            )
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val statsItems by viewModel.statsItems.collectAsState()
    val bookingData by viewModel.bookingData.collectAsState()
    val userGrowthData by viewModel.userGrowthData.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Dashboard",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            StatsSliderSection(statList = statsItems)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .padding(vertical = 12.dp),

                    thickness = 1.dp,
                    color = Color(0xFFD5D5D5)
                )
            }
            Text(
                text = "Monthly Trends",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp)
            )
        }

        item {
            TrendChartSection(bookingData, userGrowthData)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Most Popular Events🔥",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),

                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PopularEvent(
                        "Voichestra",
                        1278,
                        R.drawable.voichestra,
                        2,
                        Color(0xFF8CCAD4),
                        Color(0xFFE0F7FF)
                    )
                    PopularEvent(
                        "TARCian Run",
                        7459,
                        R.drawable.tarcian_run,
                        1,
                        Color(0xFFFC8770),
                        Color(0xFFFFE0E0)
                    )
                    PopularEvent(
                        "GOTAR",
                        958,
                        R.drawable.gotar,
                        3,
                        Color(0xFF3777D8),
                        Color(0xFFE0E7FF)
                    )
                }
            }
        }
    }
}