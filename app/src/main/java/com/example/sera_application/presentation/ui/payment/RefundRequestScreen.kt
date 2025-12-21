package com.example.sera_application.presentation.ui.payment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sera_application.BottomNavigationBar
import com.example.sera_application.ui.theme.SERA_ApplicationTheme

class RefundRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SERA_ApplicationTheme {
                RefundRequestScreen(
                    onBack = { finish() },
                    onSubmitSuccess = { finish() },
                    onHomeClick = {
                        val intent = android.content.Intent(this, com.example.sera_application.MainActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("DESTINATION", "event_list")
                        }
                        startActivity(intent)
                    },
                    onProfileClick = {
                        val intent = android.content.Intent(this, com.example.sera_application.MainActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("DESTINATION", "profile")
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundRequestScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    var showRefundReasonDialog by rememberSaveable { mutableStateOf(false) }
    var selectedReason by rememberSaveable { mutableStateOf("") }
    var additionalNotes by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Refund Request",
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
            BottomNavigationBar(
                onHomeClick = onHomeClick,
                onMeClick = onProfileClick
            )
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
            Text(
                text = "Payment Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Event",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "MUSIC FIESTA 6.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Payment Amount",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "RM 70.00",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Payment Date",
                                fontSize = 14.sp,
                                color = Color.Gray)
                            Text(
                                text = "Nov 5, 2025",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Transaction",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "1234-1234-1234",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Refund Policy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Refund Amount",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "RM 70.00",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Deadline",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "24 hours",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = "before event",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Processing Time",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "5 - 7 business",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = "days",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Important:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp),
                color = Color.Black
            )
            Text(
                text = "• Refund will be processed to your original payment method.",
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 12.dp),
                color = Color(0xFF666666)
            )
            Text(
                text = "• This action cannot be undone.",
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 12.dp),
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B9FED)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    showRefundReasonDialog = true
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
                    text = "Process",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showRefundReasonDialog) {
                RefundReasonDialog(
                    selectedReason = selectedReason,
                    onReasonChange = { selectedReason = it },
                    additionalNotes = additionalNotes,
                    onNotesChange = { additionalNotes = it },
                    onDismiss = { showRefundReasonDialog = false },
                    onSubmit = { reason, notes ->
                        showRefundReasonDialog = false
                        Toast.makeText(
                            context,
                            "Refund request submitted: $reason",
                            Toast.LENGTH_LONG
                        ).show()
                        onSubmitSuccess()
                    }
                )
            }
        }
    }
}

@Composable
fun RefundReasonDialog(
    selectedReason: String,
    onReasonChange: (String) -> Unit,
    additionalNotes: String,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,

        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Refund Reason",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column {
                        val reasons = listOf(
                            "Cannot attend the event",
                            "Event details changed",
                            "Personal reasons",
                            "Other (please specify)"
                        )

                        reasons.forEach { reason ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onReasonChange(reason) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedReason == reason,
                                    onClick = { onReasonChange(reason) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color.Black,
                                        unselectedColor = Color.Black
                                    )
                                )
                                Text(
                                    text = reason,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Additional Notes (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = onNotesChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            text = "[Text area for notes]",
                            color = Color(0xFFBDBDBD)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                    )
                )
            }
        },

        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B9FED)
                    ),
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        if (selectedReason.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Please select a refund reason",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (selectedReason == "Other (please specify)" && additionalNotes.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please provide additional notes for 'Other'",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        onSubmit(selectedReason, additionalNotes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B9FED)
                    ),
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = null
    )
}