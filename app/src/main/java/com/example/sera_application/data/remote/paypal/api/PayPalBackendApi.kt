package com.example.sera_application.data.remote.paypal.api

import com.example.sera_application.data.remote.paypal.dto.CaptureOrderRequest
import com.example.sera_application.data.remote.paypal.dto.CaptureOrderResponse
import com.example.sera_application.data.remote.paypal.dto.CreateOrderRequest
import com.example.sera_application.data.remote.paypal.dto.CreateOrderResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Backend API interface for PayPal operations.
 * Your backend server handles direct PayPal API communication.
 */
interface PayPalBackendApi {

    @POST("api/paypal/create-order")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): CreateOrderResponse

    @POST("api/paypal/capture-order")
    suspend fun captureOrder(
        @Body request: CaptureOrderRequest
    ): CaptureOrderResponse
}