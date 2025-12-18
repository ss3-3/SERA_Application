package com.example.sera_application

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sera_application.presentation.ui.payment.OrganizerPaymentManagementActivity
import com.example.sera_application.presentation.ui.payment.PaymentActivity
import com.example.sera_application.presentation.ui.payment.PaymentHistoryActivity
import com.example.sera_application.ui.theme.SERA_ApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SERA_ApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainMenu()
                }
            }
        }
    }
}

@Composable
fun MainMenu() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Payment Module (Standalone)",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, PaymentActivity::class.java))
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Go to Payment Screen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, PaymentHistoryActivity::class.java))
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Go to Payment History")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(
                    Intent(context, OrganizerPaymentManagementActivity::class.java)
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Go to Organizer Management")
        }
    }
}