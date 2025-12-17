package com.example.sera_application.data.remote.datasource

import com.example.sera_application.data.remote.paypal.PayPalCaptureResult
import com.example.sera_application.data.remote.paypal.PayPalOrderResult

/**
 * Remote data source interface for PayPal payment operations.
 * Communicates with backend API endpoints, not PayPal directly.
 */
interface PayPalRemoteDataSource {

    /**
     * Creates a PayPal order via backend API.
     * 
     * @param amount Payment amount
     * @param currency Currency code (e.g., "USD", "MYR")
     * @return PayPalOrderResult containing order ID and approval URL on success
     */
    suspend fun createOrder(
        amount: Double,
        currency: String
    ): PayPalOrderResult

    /**
     * Captures a PayPal order after user approval via backend API.
     * 
     * @param orderId PayPal order ID from createOrder response
     * @return PayPalCaptureResult containing capture details on success
     */
    suspend fun captureOrder(
        orderId: String
    ): PayPalCaptureResult
}