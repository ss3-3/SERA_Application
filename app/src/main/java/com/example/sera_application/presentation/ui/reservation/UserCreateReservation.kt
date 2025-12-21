package com.example.sera_application.presentation.ui.reservation


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sera_application.R
import com.example.sera_application.domain.model.enums.EventStatus
import coil.compose.AsyncImage
import com.example.sera_application.presentation.ui.components.SafeImageLoader
import com.example.sera_application.utils.DateTimeFormatterUtil

// Data Model
data class TicketZone(
    val name: String,
    val priceLabel: String,
    val available: Int
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationScreen(
    modifier: Modifier = Modifier,
    eventId: String, // Pass eventId instead of raw data
    onBack: () -> Unit = {},
    onReservationConfirmed: (String) -> Unit = {},
    viewModel: com.example.sera_application.presentation.viewmodel.reservation.ReservationFormViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // Load event data
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.event
    val isLoading = uiState.isLoading
    val error = uiState.error

    // Navigate on success
    LaunchedEffect(uiState.isSuccess, uiState.reservationId) {
        if (uiState.isSuccess && uiState.reservationId != null) {
            val reservationId = uiState.reservationId!!
            viewModel.clearSuccessState()
            onReservationConfirmed(reservationId)
        }
    }

    // Default values or loading state
    val eventName = event?.name ?: "Loading..."
    val eventDate = event?.let { DateTimeFormatterUtil.formatDate(it.date) } ?: "Loading..."
    val eventTime = event?.let { 
        DateTimeFormatterUtil.formatTimeRange(it.startTime, it.endTime) 
    } ?: ""
    val venue = event?.location ?: ""
    val description = event?.description ?: "No description available."

    val eventStatusLabel = event?.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Available"
    val eventStatusColor = when (event?.status) {
        EventStatus.APPROVED -> Color(0xFF4CAF50) // Green
        EventStatus.PENDING -> Color(0xFFFFC107) // Amber
        EventStatus.COMPLETED -> Color(0xFF2196F3) // Blue
        EventStatus.CANCELLED,
        EventStatus.REJECTED -> Color(0xFF757575) // Grey
        null -> Color(0xFF4CAF50) // Default for null (Loading/Available)
    }

    val zones = remember(event) {
        listOf(
            TicketZone("Rock Zone", "RM %.2f".format(event?.rockZonePrice ?: 0.0), event?.rockZoneSeats ?: 0),
            TicketZone("Normal Zone", "RM %.2f".format(event?.normalZonePrice ?: 0.0), event?.normalZoneSeats ?: 0)
        )
    }

    var quantities by remember(zones) { mutableStateOf(zones.associate { it.name to 0 }) }

    val context = LocalContext.current
    
    // Show error message if any
    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    // Purchase handler wrapper
    val onPurchase: (Map<String, Int>) -> Unit = { selectedQuantities ->
        viewModel.createReservation(
            eventId = eventId,
            quantities = selectedQuantities
        )
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val bannerHeight = 250.dp
    val buttonHeight = 68.dp // Purchase button + padding

    // Draggable offset state
    // Initial offset: card starts right below the banner
    val initialOffset = bannerHeight.value
    var offsetY by remember { mutableFloatStateOf(initialOffset) }
    val maxDragUp = (bannerHeight.value / 2) // Fully expanded (can go above half of banner)
    val minDragDown = screenHeight.value - 100.dp.value // Collapsed state (more room at bottom)

    // Animated offset for smooth transitions
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        label = "cardOffset"
    )
    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.height(240.dp)) {
            EventBannerImage(
                imagePath = event?.imagePath,
                eventName = event?.name ?: "Event",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            // Transparent circular back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 30.dp, start = 16.dp)
                    .size(42.dp)
                    .background(Color(0x66000000), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
// Draggable Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.TopStart)
                .offset(y = animatedOffsetY.dp)
        ) {
            // Combined event card with details, description, and ticket selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                // Snap to nearest position
                                val midPoint = (maxDragUp + minDragDown) / 2
                                offsetY = if (offsetY < midPoint) {
                                    maxDragUp // Snap to fully expanded
                                } else {
                                    minDragDown // Snap to collapsed state
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                val newOffset = offsetY + dragAmount
                                // Constrain the offset
                                offsetY = newOffset.coerceIn(maxDragUp, minDragDown)
                            }
                        )
                    },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = Color(0xFFE0E0E0)
                        ) {}
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Event Name and Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = eventName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (eventStatusLabel != null) {
                                StatusChip(
                                    label = eventStatusLabel,
                                    color = eventStatusColor
                                )
                            }
                        }

                        // Date and Time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$eventDate $eventTime", color = Color.Gray)
                        }

                        // Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(venue, color = Color.Gray)
                        }

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Description", fontWeight = FontWeight.SemiBold)
                            Text(description, color = Color(0xFF666666))
                        }

                        // Ticket Selection
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Ticket Selection", fontWeight = FontWeight.SemiBold)
                            zones.forEach { zone ->
                                TicketRow(
                                    zone = zone,
                                    quantity = quantities[zone.name] ?: 0,
                                    onIncrement = {
                                        val current = quantities[zone.name] ?: 0
                                        if (current < zone.available) {
                                            quantities = quantities.toMutableMap().apply {
                                                this[zone.name] = current + 1
                                            }
                                        }
                                    },
                                    onDecrement = {
                                        val current = quantities[zone.name] ?: 0
                                        if (current > 0) {
                                            quantities = quantities.toMutableMap().apply {
                                                this[zone.name] = current - 1
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Purchase Button - Fixed at bottom of screen (outside draggable card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            val totalSelected = quantities.values.sum()
            Button(
                onClick = { onPurchase(quantities) },
                enabled = !isLoading && totalSelected > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F7AE0)
                )
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Purchase",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color = Color(0xFF1F7AE0)) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun TicketRow(
    zone: TicketZone,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    "Available: ${zone.available}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Text(
                zone.priceLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            QuantitySelector(
                quantity = quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement
            )
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrement, enabled = quantity > 0) {
            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(quantity.toString(), modifier = Modifier.width(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        IconButton(onClick = onIncrement) {
            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EventBannerImage(
    imagePath: String?,
    eventName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Try to load as drawable resource first
    val imageRes = remember(imagePath) {
        if (imagePath != null && imagePath.isNotBlank()) {
            // Check if it's a drawable resource name
            val resId = context.resources.getIdentifier(
                imagePath,
                "drawable",
                context.packageName
            )
            if (resId != 0) resId else null
        } else {
            null
        }
    }
    
    when {
        // Case 1: Valid drawable resource
        imageRes != null -> {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = eventName,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        // Case 2: Try as file path
        imagePath != null && imagePath.isNotBlank() -> {
            SafeImageLoader(
                imagePath = imagePath,
                contentDescription = eventName,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        // Case 3: No image - show placeholder
        else -> {
            Box(
                modifier = modifier
                    .background(Color(0xFF1A237E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = eventName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}