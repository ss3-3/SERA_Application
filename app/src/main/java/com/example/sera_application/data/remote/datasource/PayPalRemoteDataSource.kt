package com.example.sera_application.data.remote.datasource

import com.example.sera_application.domain.model.PayPalOrderResult

/**
 * Remote data source interface for PayPal payment operations.
 *
 * This layer talks only to the backend (or a future gateway service),
 * never directly to the PayPal SDK or Android UI.
 */
interface PayPalRemoteDataSource {

    /**
     * Creates a PayPal order via backend API.
     *
     * @param amount Payment amount
     * @param currency Currency code (e.g., "USD", "MYR")
     * @return Domain-level PayPalOrderResult describing the outcome.
     */
    suspend fun createOrder(
        amount: Double,
        currency: String
    ): PayPalOrderResult

    /**
     * Captures a PayPal order after user approval via backend API.
     *
     * @param orderId PayPal order ID from createOrder response
     * @return Domain-level PayPalOrderResult describing the outcome.
     */
    suspend fun captureOrder(
        orderId: String
    ): PayPalOrderResult
}
