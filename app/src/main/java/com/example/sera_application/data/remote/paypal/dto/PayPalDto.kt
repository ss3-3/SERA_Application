package com.example.sera_application.data.remote.paypal.dto

/**
 * Data Transfer Objects for communication with backend PayPal API.
 * These DTOs represent the request/response structure expected by the backend.
 */

/**
 * Request DTO for creating a PayPal order.
 */
data class CreateOrderRequest(
    val amount: Double,
    val currency: String
)

/**
 * Response DTO for creating a PayPal order.
 */
data class CreateOrderResponse(
    val success: Boolean,
    val orderId: String?,
    val approvalUrl: String?,
    val error: String? = null
)

/**
 * Request DTO for capturing a PayPal order.
 */
data class CaptureOrderRequest(
    val orderId: String
)

/**
 * Response DTO for capturing a PayPal order.
 */
data class CaptureOrderResponse(
    val success: Boolean,
    val captureId: String?,
    val status: String?,
    val amount: Double?,
    val currency: String?,
    val error: String? = null
)