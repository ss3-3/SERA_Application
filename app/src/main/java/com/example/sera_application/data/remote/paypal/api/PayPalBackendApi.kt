package com.example.sera_application.data.remote.paypal.api

import com.example.sera_application.data.remote.paypal.dto.CaptureOrderRequest
import com.example.sera_application.data.remote.paypal.dto.CaptureOrderResponse
import com.example.sera_application.data.remote.paypal.dto.CreateOrderRequest
import com.example.sera_application.data.remote.paypal.dto.CreateOrderResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for PayPal backend endpoints.
 *
 * Assumes backend endpoints:
 * - POST /paypal/create-order
 * - POST /paypal/capture-order
 *
 * Note: Backend handles OAuth2 token retrieval and PayPal API communication.
 * Android app only communicates with backend, never directly with PayPal.
 */
interface PayPalBackendApi {

    /**
     * Creates a PayPal order via backend.
     * Backend handles OAuth2 token and PayPal Order API call.
     */
    @POST("paypal/create-order")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): CreateOrderResponse

    /**
     * Captures a PayPal order via backend.
     * Backend handles OAuth2 token and PayPal Capture API call.
     */
    @POST("paypal/capture-order")
    suspend fun captureOrder(
        @Body request: CaptureOrderRequest
    ): CaptureOrderResponse
}