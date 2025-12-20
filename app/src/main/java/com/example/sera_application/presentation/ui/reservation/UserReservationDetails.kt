package com.example.sera_application.presentation.ui.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sera_application.utils.DateTimeFormatterUtil
import com.example.sera_application.utils.DateTimeFormatterUtil.formatTimeRange

data class UserReservationDetailUiModel(
    val reservationId: String,
    val eventName: String,
    val venue: String,
    val eventDate: String,
    val eventTime: String,
    val seatNumbers: String,
    val status: com.example.sera_application.domain.model.enums.ReservationStatus,
    val transactionDate: String,
    val transactionTime: String,
    val transactionId: String,
    val paymentMethod: String,
    val zoneName: String,
    val quantity: Int,
    val totalPrice: String,
    val pricePerSeat: String,
    val qrCodeData: String = "" // Reservation ID or transaction ID for QR code
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReservationDetailScreen(
    reservationId: String, // Changed from reservation object to ID
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onCancelReservation: () -> Unit = {},
    viewModel: com.example.sera_application.presentation.viewmodel.reservation.ReservationDetailsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(reservationId) {
        viewModel.loadReservation(reservationId)
    }

    val uiState by viewModel.uiState.collectAsState()
    
    val reservationVal = uiState.reservation
    val eventVal = uiState.event
    
    if (uiState.isLoading) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (reservationVal != null) {
        val uiModel = remember(reservationVal, eventVal) {
             UserReservationDetailUiModel(
                reservationId = reservationVal.reservationId,
                eventName = eventVal?.name ?: "Unknown Event",
                venue = eventVal?.location ?: "Unknown Venue",
                 eventDate = eventVal?.let {
                     DateTimeFormatterUtil.formatDate(it.date)
                 } ?: "",
                 eventTime = if (eventVal != null) {
                     DateTimeFormatterUtil.formatTimeRange(
                         eventVal.startTime,
                         eventVal.endTime
                     )
                 } else "",
                seatNumbers = "${reservationVal.seats} Seats",
                status = reservationVal.status,
                transactionDate = java.text.SimpleDateFormat("dd MMM yyyy").format(java.util.Date(reservationVal.createdAt)),
                transactionTime = java.text.SimpleDateFormat("hh:mm a").format(java.util.Date(reservationVal.createdAt)),
                transactionId = reservationVal.reservationId, // Using reservation ID as transaction ID
                paymentMethod = "Credit Card",
                zoneName = "General",
                quantity = reservationVal.seats,
                totalPrice = "RM ${(eventVal?.priceRange?.filter { it.isDigit() }?.toIntOrNull() ?: 10) * reservationVal.seats}",
                pricePerSeat = eventVal?.priceRange ?: "RM 10"
            )
        }
    
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
                // Reservation Info Card
                UserReservationInfoCard(uiModel)

                // Order Details Card
                UserOrderDetailsCard(uiModel)

                // Seats Card
                UserSeatsCard(uiModel)

                // QR Code
                QRCodeSection(uiModel.qrCodeData.ifEmpty { uiModel.reservationId })

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel Reservation Button
                Button(
                    onClick = onCancelReservation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE3F2FD), // Light blue
                        contentColor = Color(0xFF1976D2) // Dark blue text
                    ),
                    enabled = uiModel.status == com.example.sera_application.domain.model.enums.ReservationStatus.CONFIRMED ||
                            uiModel.status == com.example.sera_application.domain.model.enums.ReservationStatus.PENDING
                ) {
                    Text(
                        "Cancel Reservation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    } else {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Reservation not found.")
        }
    }
}

@Composable
private fun UserReservationInfoCard(reservation: UserReservationDetailUiModel) {
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

            InfoRowWithIcon(
                icon = Icons.Default.LocationOn,
                label = reservation.venue,
                iconTint = Color(0xFF757575)
            )

            InfoRowWithIcon(
                icon = Icons.Default.CalendarToday,
                label = "${reservation.eventDate}, ${reservation.eventTime}",
                iconTint = Color(0xFF757575)
            )

            InfoRowWithIcon(
                icon = Icons.Default.Info,
                label = reservation.seatNumbers,
                iconTint = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun UserOrderDetailsCard(reservation: UserReservationDetailUiModel) {
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
                "Order Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Divider(color = Color(0xFFE0E0E0))

            InfoRow("Transaction Date", reservation.transactionDate)
            InfoRow("Transaction Time", reservation.transactionTime)
            InfoRow("Transaction ID", reservation.transactionId)
            InfoRow("Payment Method", reservation.paymentMethod)
        }
    }
}

@Composable
private fun UserSeatsCard(reservation: UserReservationDetailUiModel) {
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
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        reservation.totalPrice,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        reservation.pricePerSeat,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
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

@Composable
private fun QRCodeSection(qrCodeData: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // QR Code placeholder - replace with actual QR code generation
            // You can use a library like 'com.journeyapps:zxing-android-embedded'
            // or generate QR code using Canvas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "QR Code\n($qrCodeData)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}