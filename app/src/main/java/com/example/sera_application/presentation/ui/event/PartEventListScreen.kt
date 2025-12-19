package com.example.sera_application.presentation.ui.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.sera_application.R
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.presentation.viewmodel.event.EventListViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

// UI-specific model for displaying events
data class EventDisplayModel(
    val id: String,
    val name: String,
    val organizer: String,
    val date: String,
    val time: String,
    val venue: String,
    val priceRange: String,
    val availableSeats: Int,
    val totalSeats: Int,
    val category: EventCategoryUI,
    val description: String,
    val bannerUrl: String? = null, // for Firebase/server
) {
    companion object {
         // Convert domain Event to UI EventDisplayModel
        fun fromDomain(event: Event): EventDisplayModel {
            return EventDisplayModel(
                id = event.eventId,
                name = event.name,
                organizer = event.organizerName,
                date = event.date,
                time = event.timeRange,
                venue = event.location,
                priceRange = event.priceRange,
                availableSeats = event.availableSeats,
                totalSeats = event.totalSeats,
                category = EventCategoryUI.fromDomain(event.category),
                description = event.description,
                bannerUrl = event.imagePath
            )
        }
    }
}

// UI-specific event category with icons
enum class EventCategoryUI(
    val displayName: String,
    val icon: ImageVector,
    val domainCategory: EventCategory
) {
    ACADEMIC("Academic", Icons.Default.MenuBook, EventCategory.ACADEMIC),
    CAREER("Career", Icons.Default.BusinessCenter, EventCategory.CAREER),
    ART("Art", Icons.Default.Palette, EventCategory.ART),
    WELLNESS("Wellness", Icons.Default.FavoriteBorder, EventCategory.WELLNESS),
    MUSIC("Music", Icons.Default.MusicNote, EventCategory.MUSIC),
    FESTIVAL("Festival", Icons.Default.Celebration, EventCategory.FESTIVAL);

    companion object {
        fun fromDomain(category: EventCategory): EventCategoryUI {
            return values().first { it.domainCategory == category }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    modifier: Modifier = Modifier,
    onEventClick: (String) -> Unit = {}, // Pass event ID
    onProfileClick: () -> Unit = {},
    viewModel: EventListViewModel = hiltViewModel()
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<EventCategoryUI?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    
    // Load events on first composition
    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    val events = uiState.events.map { EventDisplayModel.fromDomain(it) }

    val filteredEvents = events.filter { event ->
        val matchesSearch = event.name.contains(searchQuery, ignoreCase = true) ||
                event.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || event.category == selectedCategory
        matchesSearch && matchesCategory
    }
    
    // Show loading/error states
    if (uiState.isLoading && events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading events...", color = Color.Gray)
        }
        return
    }
    
    if (uiState.errorMessage != null && events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.errorMessage ?: "Failed to load events",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { viewModel.loadEvents() }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Logo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.tarumt_logo_transparent),
                                contentDescription = "TARUMT Logo",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search", fontSize = 12.sp, color = Color.LightGray.copy(alpha = 0.6f)) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(13.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.LightGray,
                                unfocusedTextColor = Color.LightGray,
                                cursorColor = Color.LightGray,
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Notification (future: connect to notification screen)
                        IconButton(onClick = { /* handle notification click */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Me") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Event Banner
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(events.take(3)) { event ->
                        EventBanner(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Category Filter Section
            item {
                Text(
                    text = "Choose By Category",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Grid layout for categories (2 rows x 3 columns)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    val categories = EventCategoryUI.values().toList()
                    categories.chunked(3).forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCategories.forEach { category ->
                                CategoryChip(
                                    category = category,
                                    isSelected = selectedCategory == category,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == category) null else category
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty spaces if less than 3 items
                            repeat(3 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Event Cards List
            items(filteredEvents.chunked(2)) { rowEvents ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    rowEvents.forEach { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Add empty space if odd number of items
                    if (rowEvents.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EventBanner(
    event: EventDisplayModel?,
    onClick: () -> Unit
) {
    if (event == null) return

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF0D47A1)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: EventCategoryUI,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5))
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.displayName,
            tint = if (isSelected) Color(0xFF2196F3) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.displayName,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF2196F3) else Color.Gray,
            maxLines = 1, // // only display in 1 line
            overflow = TextOverflow.Ellipsis, // if text too long then display "Welcome..."
            textAlign = TextAlign.Center
        )

    }
}

@Composable
private fun EventCard(
    event: EventDisplayModel,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Event Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = Color(0xFF1A1A2E),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Load actual image from bannerUrl
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
                    Text(
                        text = event.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Event Info
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = event.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date & Time Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date}, ${event.time}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Venue Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        tint = Color(0xFFFFA726), // change icon/image color
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.venue,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Join Now Button
                Button(
                    onClick = onClick,
                    modifier = Modifier.width(115.dp).height(35.dp).align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("JOIN NOW", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun drawableFromName(name: String): Int? {
    val context = LocalContext.current
    val resId = remember(name) {
        context.resources.getIdentifier(
            name,
            "drawable",
            context.packageName
        )
    }
    return if (resId != 0) resId else null
}

// Sample data function (temporary)
private fun getSampleEvents(): List<EventDisplayModel> {
    return listOf(
        EventDisplayModel(
            id = "1",
            name = "MUSIC FIESTA 6.0",
            organizer = "Organizer1",
            date = "12/01/2026",
            time = "19:00 PM - 22:00 PM", // TODO: Replace with actual time?
            venue = "Rimba, TARUMT",
            priceRange = "RM 35 - RM 60", // TODO: Replace with actual INT?
//            vipSeats = 80,
//            normalSeats = 320,
            availableSeats = 180, // TODO: Replace with actual seats (minus from...)
            totalSeats = 400,
            category = EventCategoryUI.MUSIC,
            description = "Music Fiesta 6.0 is a large-scale campus concert and carnival proudly organized by Music Society of Tunku Abdul Rahman University of Management and Technology (TARUMT).",
            bannerUrl = "musicfiesta"
        ),
        EventDisplayModel(
            id = "2",
            name = "GOTAR Festival",
            organizer = "Organizer2",
            date = "15/11/2025",
            time = "9:00 AM",
            venue = "Arena, TARUMT",
            priceRange = "RM 20 - RM 40",
            availableSeats = 150,
            totalSeats = 200,
            category = EventCategoryUI.FESTIVAL,
            description = "Annual festival celebrating culture and arts.",
            bannerUrl = "gotar"
        ),
        EventDisplayModel(
            id = "3",
            name = "VOICHESTRA",
            organizer = "Organizer3",
            date = "8/12/2025",
            time = "7:30 PM",
            venue = "Keris Hall, TARUMT",
            priceRange = "RM 25",
            availableSeats = 100,
            totalSeats = 120,
            category = EventCategoryUI.MUSIC,
            description = "A cappella choir performance event.",
            bannerUrl = "voichstra"
        ),
        EventDisplayModel(
            id = "4",
            name = "Internship Fair",
            organizer = "Organizer4",
            date = "18/11/2025",
            time = "10:00 AM",
            venue = "DTAR",
            priceRange = "Free",
            availableSeats = 300,
            totalSeats = 300,
            category = EventCategoryUI.CAREER,
            description = "Connect with top employers for internship opportunities.",
            bannerUrl = "sodc"
        )
    )
}

// ==================== PREVIEW ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EventListScreenPreview() {
    val sampleEvents = getSampleEvents()
    // Use a simple preview of the UI using sample data
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<EventCategoryUI?>(null) }

    val filteredEvents = sampleEvents.filter { event ->
        val matchesSearch = event.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || event.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = { },
        bottomBar = { }
    ) { padding ->
        // Reuse main content layout for preview
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(filteredEvents.chunked(2)) { rowEvents ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowEvents.forEach { event ->
                        EventCard(
                            event = event,
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowEvents.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
