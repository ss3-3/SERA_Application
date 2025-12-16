package com.example.sera_application.presentation.ui.reservation

import com.example.sera_application.domain.model.enums.ReservationStatus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Extension properties for ReservationStatus
val ReservationStatus.label: String
    get() = when (this) {
        ReservationStatus.PENDING -> "Pending"
        ReservationStatus.CONFIRMED -> "Confirmed"
        ReservationStatus.CANCELLED -> "Cancelled"
        ReservationStatus.COMPLETED -> "Completed"
    }

val ReservationStatus.color: Color
    get() = when (this) {
        ReservationStatus.PENDING -> Color(0xFFFFC107)
        ReservationStatus.CONFIRMED -> Color(0xFF4CAF50)
        ReservationStatus.CANCELLED -> Color(0xFFF44336)
        ReservationStatus.COMPLETED -> Color(0xFF2196F3)
    }

data class ReservationDetailUiModel(
    val reservationId: String,
    val participantName: String,
    val participantEmail: String,
    val eventName: String,
    val venue: String,
    val eventDate: String,
    val eventTime: String,
    val seatNumbers: String,
    val status: ReservationStatus,
    val paymentMethod: String,
    val paymentAccount: String, // Masked account number like "1234-1234-1234"
    val zoneName: String,
    val quantity: Int,
    val totalPrice: String,
    val pricePerSeat: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservation: ReservationDetailUiModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Reservation Detail",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2C2C2E) // Dark grey header
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Light grey background
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Participant Info Card
            ParticipantInfoCard(reservation)

            // Reservation Info Card
            ReservationInfoCard(reservation)

            // Seats Card
            SeatsCard(reservation)

            // Payment Card
            PaymentCard(reservation)
        }
    }
}

@Composable
private fun ParticipantInfoCard(reservation: ReservationDetailUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Participant Info",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Divider(color = Color(0xFFE0E0E0))

            InfoRow("Name:", reservation.participantName)
            InfoRow("Email:", reservation.participantEmail)
        }
    }
}

@Composable
private fun ReservationInfoCard(reservation: ReservationDetailUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Reservation Info",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                // Status badge with background
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = reservation.status.color.copy(alpha = 0.15f),
                    contentColor = reservation.status.color
                ) {
                    Text(
                        reservation.status.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(color = Color(0xFFE0E0E0))

            InfoRow("Reservation ID:", reservation.reservationId)
            InfoRow("Event Name:", reservation.eventName)
            InfoRow("Location:", reservation.venue)
            InfoRow("Date and Time:", "${reservation.eventDate}, ${reservation.eventTime}")
            InfoRow("Location:", reservation.seatNumbers)
        }
    }
}

@Composable
private fun PaymentCard(reservation: ReservationDetailUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Divider(color = Color(0xFFE0E0E0))

            InfoRow(reservation.paymentMethod, reservation.paymentAccount)
        }
    }
}

@Composable
private fun SeatsCard(reservation: ReservationDetailUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Seats",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Divider(color = Color(0xFFE0E0E0))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reservation.zoneName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${reservation.quantity}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        reservation.pricePerSeat,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Subtotal",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        reservation.totalPrice,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color(0xFF757575),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoRowWithIcon(
    icon: ImageVector,
    label: String,
    iconTint: Color = Color(0xFF757575)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ReservationDetailScreenPreview() {
    MaterialTheme {
        ReservationDetailScreen(
            reservation = ReservationDetailUiModel(
                reservationId = "XXX123",
                participantName = "Haha haha ha",
                participantEmail = "haha@gmail.com",
                eventName = "Music Fiesta 6.0",
                venue = "Rimba, TARUMT",
                eventDate = "8 Nov 2025",
                eventTime = "6 PM",
                seatNumbers = "A12, A13",
                status = ReservationStatus.CONFIRMED,
                paymentMethod = "PayPal",
                paymentAccount = "1234-1234-1234",
                zoneName = "NORMAL ZONE",
                quantity = 2,
                totalPrice = "RM 70.00",
                pricePerSeat = "RM 35.00"
            )
        )
    }
}

