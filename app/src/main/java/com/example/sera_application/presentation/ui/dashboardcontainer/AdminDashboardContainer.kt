package com.example.sera_application.presentation.ui.dashboardcontainer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import com.example.sera_application.presentation.ui.dashboard.AdminDashboardScreen
import com.example.sera_application.presentation.ui.report.EventReportScreen
import com.example.sera_application.presentation.ui.report.RevenueReportScreen
import com.example.sera_application.presentation.ui.report.UserReportScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContainer(
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Dashboard",
        "Event Report",
        "User Report",
        "Revenue Report"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Admin Reports",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
        ) {
            // Simple Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF1A1A2E),
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (selectedTabIndex == index) Color(0xFF1A1A2E) else Color(0xFF757575)
                            ) 
                        }
                    )
                }
            }

            // Content based on selected tab with smooth transitions
            // Only create the ViewModel for the currently selected tab to avoid loading all data at once
            androidx.compose.runtime.DisposableEffect(selectedTabIndex) {
                Log.d("AdminDashboardContainer", "Tab changed to index: $selectedTabIndex")
                onDispose {
                    Log.d("AdminDashboardContainer", "Tab disposed: $selectedTabIndex")
                }
            }
            
            when (selectedTabIndex) {
                0 -> AdminDashboardScreen()
                1 -> EventReportScreen()
                2 -> UserReportScreen()
                3 -> RevenueReportScreen()
            }
        }
    }
}