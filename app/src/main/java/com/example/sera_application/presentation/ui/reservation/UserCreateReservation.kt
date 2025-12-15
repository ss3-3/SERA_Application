package com.example.sera_application.presentation.ui.reservation


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sera_application.R

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
    eventName: String,
    eventDate: String,
    eventTime: String,
    venue: String,
    description: String,
    zones: List<TicketZone>,
    eventStatusLabel: String? = null,
    eventStatusColor: Color = Color(0xFF1F7AE0),
    onBack: () -> Unit = {},
    onPurchase: (Map<String, Int>) -> Unit = {}
) {
    var quantities by remember { mutableStateOf(zones.associate { it.name to 0 }) }

    Column(modifier = modifier.fillMaxSize()) {

        Box(modifier = Modifier.height(240.dp)) {

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // <--- replace with your banner
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // Transparent circular back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
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
// Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Combined event card with details, description, and ticket selection
            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = eventName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (eventStatusLabel != null) {
                            StatusChip(
                                label = eventStatusLabel,
                                color = eventStatusColor
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$eventDate  $eventTime", color = Color.Gray)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(venue, color = Color.Gray)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Description", fontWeight = FontWeight.SemiBold)
                        Text(description, color = Color(0xFF666666))
                    }

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

                Button(
                    onClick = { onPurchase(quantities) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F7AE0)
                        //contentColor = Color.White
                    )
                ) {
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