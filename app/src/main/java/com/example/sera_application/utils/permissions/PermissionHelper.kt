package com.example.sera_application.utils.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

/**
 * Check if notification permission is granted
 */
fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // For Android 12 and below, notification permission is granted by default
        true
    }
}

/**
 * Composable function to handle notification permission request
 * 
 * @param onPermissionGranted Callback when permission is granted
 * @param onPermissionDenied Callback when permission is denied (composable context)
 * @param showRationale Whether to show rationale dialog before requesting
 * @return Triple of (hasPermission: Boolean, requestPermission: () -> Unit, showRationaleDialog: Boolean)
 */
@Composable
fun rememberNotificationPermissionState(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: @Composable () -> Unit = {},
    showRationale: Boolean = false
): Triple<Boolean, () -> Unit, Boolean> {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(hasNotificationPermission(context))
    }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var permissionDeniedTrigger by remember { mutableStateOf(0) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        showRationaleDialog = false
        if (isGranted) {
            onPermissionGranted()
        } else {
            // Trigger state change to handle in composable context
            permissionDeniedTrigger++
        }
    }
    
    // Handle permission denied in composable context
    // Use LaunchedEffect to handle the trigger
    LaunchedEffect(permissionDeniedTrigger) {
        if (permissionDeniedTrigger > 0) {
            // The actual composable handling will be done by the caller
            // We just track the state change here
        }
    }
    
    // Call the composable function when permission is denied
    if (permissionDeniedTrigger > 0) {
        onPermissionDenied()
    }
    
    val requestPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission) {
                // Check if we should show rationale
                val shouldShowRationale = androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                    context as android.app.Activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                
                if (shouldShowRationale && showRationale) {
                    showRationaleDialog = true
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                onPermissionGranted()
            }
        } else {
            // For Android 12 and below, permission is granted by default
            hasPermission = true
            onPermissionGranted()
        }
    }
    
    // Rationale Dialog
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Notification Permission Required") },
            text = {
                Text(
                    "This app needs notification permission to keep you updated about " +
                            "event updates, reservation confirmations, and payment status. " +
                            "Please grant the permission to continue."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    return Triple(hasPermission, requestPermission, showRationaleDialog)
}

