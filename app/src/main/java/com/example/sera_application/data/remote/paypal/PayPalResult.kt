package com.example.sera_application.data.remote.paypal

/**
 * Result wrapper for PayPal order creation
 */
sealed class PayPalOrderResult {
    data class Success(
        val orderId: String,
        val approvalUrl: String
    ) : PayPalOrderResult()

    data class Error(
        val error: String,
        val cause: Throwable? = null
    ) : PayPalOrderResult()
}

/**
 * Result wrapper for PayPal order capture
 */
sealed class PayPalCaptureResult {
    data class Success(
        val captureId: String,
        val status: String,
        val amount: Double,
        val currency: String
    ) : PayPalCaptureResult()

    data class Error(
        val error: String,
        val cause: Throwable? = null
    ) : PayPalCaptureResult()
}