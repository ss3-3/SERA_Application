package com.example.sera_application.data.remote.paypal.dto

import com.google.gson.annotations.SerializedName

// Request DTOs
data class CreateOrderRequest(
    val amount: Double,
    val currency: String
)

data class CaptureOrderRequest(
    @SerializedName("order_id")
    val orderId: String
)

// Response DTOs
data class CreateOrderResponse(
    val success: Boolean,
    @SerializedName("order_id")
    val orderId: String? = null,
    @SerializedName("approval_url")
    val approvalUrl: String? = null,
    val error: String? = null
)

data class CaptureOrderResponse(
    val success: Boolean,
    @SerializedName("capture_id")
    val captureId: String? = null,
    val status: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val error: String? = null
)