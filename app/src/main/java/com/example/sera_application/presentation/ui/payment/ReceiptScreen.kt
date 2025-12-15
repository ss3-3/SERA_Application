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
import com.example.sera_application.BottomNavigationBar
import com.example.sera_application.R
import com.example.sera_application.ui.theme.SERA_ApplicationTheme
import com.example.sera_application.utils.PdfReceiptGenerator
import com.example.sera_application.utils.ReceiptData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SERA_ApplicationTheme {
                ReceiptScreen(
                    onDownloadReceipt = {
                        generateAndOpenPdf()
                    }
                )
            }
        }
    }

    private fun generateAndOpenPdf() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@ReceiptActivity, "Generating receipt...", Toast.LENGTH_SHORT).show()

                val pdfFile = withContext(Dispatchers.IO) {
                    val receiptData = ReceiptData(
                        eventName = "MUSIC FIESTA 6.0",
                        transactionId = "1234-1234-1234",
                        date = "8 Nov 2025",
                        time = "7:00 PM",
                        venue = "Rimba, TARUMT",
                        ticketType = "NORMAL",
                        quantity = 2,
                        seats = "H15, H16",
                        price = 70.0,
                        email = "haha@gmail.com",
                        name = "Lim Siau Siau",
                        phone = "+60 123456789"
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

                startActivity(intent)

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
fun ReceiptScreen(onDownloadReceipt: () -> Unit) {
    val context = LocalContext.current

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
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
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
            BottomNavigationBar()
        }
    ) { paddingValues ->
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
                        Image(
                            painter = painterResource(id = R.drawable.music_fiesta),
                            contentDescription = "Event Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
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
                                text = "MUSIC FIESTA 6.0",
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
                                text = "H15, H16",
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
                                text = "8 Nov 2025",
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
                                text = "Rimba, TARUMT",
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
                                text = "NORMAL",
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
                                text = "2",
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
                                text = "RM 70.00",
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
                    ReceiptDetailRow("Transaction ID", "1234-1234-1234")
                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptDetailRow("Email", "haha@gmail.com")
                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptDetailRow("Name", "Lim Siau Siau")
                    Spacer(modifier = Modifier.height(12.dp))

                    ReceiptDetailRow("Phone", "+60 123456789")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val intent = Intent(context, RefundRequestActivity::class.java)
                    context.startActivity(intent)
                },
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