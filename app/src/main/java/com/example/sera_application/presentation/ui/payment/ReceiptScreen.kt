package com.example.sera_application.presentation.ui.payment


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import com.example.sera_application.R
import com.example.sera_application.ui.theme.SERA_ApplicationTheme
import com.example.sera_application.utils.PdfReceiptGenerator
import com.example.sera_application.utils.ReceiptData
import com.example.sera_application.utils.BottomNavigationBar
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sera_application.presentation.viewmodel.user.ProfileViewModel
import com.example.sera_application.domain.model.enums.UserRole
import androidx.compose.runtime.LaunchedEffect
import com.example.sera_application.presentation.viewmodel.payment.PaymentScreenViewModel
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.sera_application.MainActivity


@AndroidEntryPoint
class ReceiptActivity : ComponentActivity() {
    private val viewModel: PaymentScreenViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: "Unknown"
        val reservationId = intent.getStringExtra("RESERVATION_ID")


        if (reservationId != null) {
            viewModel.loadReservationDetails(reservationId)
        }


        setContent {
            SERA_ApplicationTheme {
                val reservation by viewModel.reservation.collectAsState()
                val event by viewModel.event.collectAsState()
                val user by viewModel.user.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val error by viewModel.error.collectAsState()


                ReceiptScreen(
                    reservation = reservation,
                    event = event,
                    user = user,
                    transactionId = transactionId,
                    isLoading = isLoading,
                    error = error,
                    onBack = {
                        // Navigate to event list (participant home)
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("DESTINATION", "event_list")
                        }
                        startActivity(intent)
                        finish()
                    },
                    onDownloadReceipt = {
                        generateAndOpenPdf(transactionId)
                    },
                    onRequestRefund = {
                        val intent = Intent(this, RefundRequestActivity::class.java).apply {
                            putExtra("PAYMENT_ID", transactionId)
                        }
                        startActivity(intent)
                    },
                    onHomeClick = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("DESTINATION", "event_list")
                        }
                        startActivity(intent)
                        finish()
                    },
                    onProfileClick = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("DESTINATION", "profile")
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }


    private fun generateAndOpenPdf(transactionId: String) {
        val reservation = viewModel.reservation.value
        val event = viewModel.event.value
        val user = viewModel.user.value


        if (reservation == null || event == null) {
            Toast.makeText(this, "Data still loading, please wait", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {
            try {
                Toast.makeText(this@ReceiptActivity, "Generating receipt...", Toast.LENGTH_SHORT).show()


                val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                val formattedDate = dateFormat.format(java.util.Date(event.date))


                val pdfFile: java.io.File = withContext(Dispatchers.IO) {
                    val receiptData = ReceiptData(
                        eventName = event.name,
                        transactionId = transactionId,
                        date = formattedDate,
                        time = "7:00 PM", // Using default as time isn't fully formatted in Event yet
                        venue = event.location, // 'venue' -> 'location'
                        ticketType = "Standard", // 'ticketType' not in Reservation, defaulting
                        quantity = reservation.seats, // 'numberOfTickets' -> 'seats' (confirmed Int)
                        seats = "${reservation.seats} Tickets", // 'seats' is quantity in Reservation
                        price = reservation.totalPrice,
                        email = user?.email ?: "Not provided",
                        name = user?.fullName ?: "Sera User",
                        phone = user?.phone ?: "Not provided"
                    )


                    val generator = PdfReceiptGenerator(this@ReceiptActivity)
                    generator.generateReceipt(receiptData)
                }


                val uri = FileProvider.getUriForFile(
                    this@ReceiptActivity,
                    "${applicationContext.packageName}.fileprovider",
                    pdfFile
                )




                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }


                try {
                    startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    // No PDF viewer installed, offer to share instead
                    Toast.makeText(
                        this@ReceiptActivity,
                        "No PDF viewer installed. Attempting to share...",
                        Toast.LENGTH_LONG
                    ).show()


                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Open PDF with..."))
                }


            } catch (e: Exception) {
                Toast.makeText(
                    this@ReceiptActivity,
                    "Error generating receipt: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    reservation: EventReservation?,
    event: Event?,
    user: User?,
    transactionId: String,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onDownloadReceipt: () -> Unit,
    onRequestRefund: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    val context = LocalContext.current
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    val imagePath = event?.imagePath
    val imageRes = remember(imagePath) {
        if (!imagePath.isNullOrBlank()) {
            context.resources.getIdentifier(
                imagePath,
                "drawable",
                context.packageName
            )
        } else 0
    }
    
    // Get current user for role-based navigation
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()
    
    // Load current user
    LaunchedEffect(Unit) {
        profileViewModel.loadCurrentUser()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Receipt",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            )
        },
        bottomBar = {
            navController?.let { nav ->
                BottomNavigationBar(
                    navController = nav,
                    currentRoute = nav.currentBackStackEntry?.destination?.route,
                    userRole = currentUser?.role ?: UserRole.PARTICIPANT
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = error, color = Color.Red)
            }
        } else if (reservation != null && event != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (imageRes != 0) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = "Event Banner",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (!imagePath.isNullOrEmpty()) {
                                AsyncImage(
                                    model = imagePath,
                                    contentDescription = "Event Banner",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text(
                                    text = "Event",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = event.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }


                            Column(
                                modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Seat",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "General Admission", // No seat numbers in Reservation model
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text(
                                    text = "Date",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = dateFormat.format(java.util.Date(event.date)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }


                            Column(
                                modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Time",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "7:00 PM",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text(
                                    text = "Venue",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = event.location, // 'venue' -> 'location'
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }


                            Column(
                                modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Ticket Type",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "Standard", // Defaulting as info not in Reservation
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(0.5f)) {
                                Text(
                                    text = "Quantity",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = reservation.seats.toString(), // 'seats' is quantity
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }


                            Column(
                                modifier = Modifier.weight(0.5f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Price",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "RM ${String.format("%.2f", reservation.totalPrice)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ReceiptDetailRow("Transaction ID", transactionId)
                        Spacer(modifier = Modifier.height(12.dp))


                        ReceiptDetailRow("Email", user?.email ?: "Loading...")
                        Spacer(modifier = Modifier.height(12.dp))


                        ReceiptDetailRow("Name", user?.fullName ?: "Loading...")
                        Spacer(modifier = Modifier.height(12.dp))


                        ReceiptDetailRow("Phone", user?.phone ?: "Not provided")
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))


                Button(
                    onClick = onRequestRefund,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B9FED)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Request Refund",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))


                Button(
                    onClick = onDownloadReceipt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B9FED)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Download Receipt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}




@Composable
fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
