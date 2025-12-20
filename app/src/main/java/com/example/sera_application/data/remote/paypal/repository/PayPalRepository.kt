package com.example.sera_application.data.remote.paypal.repository

import android.util.Base64
import android.util.Log
import com.example.sera_application.data.remote.paypal.api.Amount
import com.example.sera_application.data.remote.paypal.api.ApplicationContext
import com.example.sera_application.data.remote.paypal.api.PayPalApiService
import com.example.sera_application.data.remote.paypal.api.PayPalCaptureResponse
import com.example.sera_application.data.remote.paypal.api.PayPalOrderRequest
import com.example.sera_application.data.remote.paypal.api.PayPalOrderResponse
import com.example.sera_application.data.remote.paypal.api.PurchaseUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PayPalRepository(
    private val clientId: String,
    private val clientSecret: String,
    private val isSandbox: Boolean = true
) {
    private val apiService = PayPalApiService.create(isSandbox)
    private var accessToken: String? = null
    private val TAG = "PayPalRepository"

    @Suppress("NewApi")
    private fun getBasicAuthHeader(): String {
        val credentials = "$clientId:$clientSecret"
        val encoded = Base64.encodeToString(
            credentials.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        return "Basic $encoded"
    }

    private fun getBearerAuthHeader(): String {
        return "Bearer $accessToken"
    }

    suspend fun authenticate(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Authenticating with PayPal...")
            val response = apiService.getAccessToken(
                basicAuth = getBasicAuthHeader(),
                grantType = "client_credentials"
            )
            
            if (response.isSuccessful && response.body() != null) {
                accessToken = response.body()!!.accessToken
                Log.d(TAG, "Authentication successful")
                Result.success(accessToken!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Authentication failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        amount: String,
        currencyCode: String = "MYR",
        description: String = "Event Ticket Purchase"
    ): Result<PayPalOrderResponse> = withContext(Dispatchers.IO) {
        try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authenticated"))
            }

            Log.d(TAG, "Creating order: Amount=$amount $currencyCode")

            val orderRequest = PayPalOrderRequest(
                intent = "CAPTURE",
                purchaseUnits = listOf(
                    PurchaseUnit(
                        amount = Amount(
                            currencyCode = currencyCode,
                            value = amount
                        ),
                        description = description
                    )
                ),
                applicationContext = ApplicationContext(
                    returnUrl = "sera://paypal.return",
                    cancelUrl = "sera://paypal.cancel"
                )
            )

            val response = apiService.createOrder(
                bearerToken = getBearerAuthHeader(),
                orderRequest = orderRequest
            )

            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!
                Log.d(TAG, "Order created successfully: ${order.id}")
                Result.success(order)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Order creation failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Order creation failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Order creation error", e)
            Result.failure(e)
        }
    }

    suspend fun captureOrder(orderId: String): Result<PayPalCaptureResponse> = withContext(Dispatchers.IO) {
        try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authenticated"))
            }

            Log.d(TAG, "Capturing order: $orderId")

            val response = apiService.captureOrder(
                bearerToken = getBearerAuthHeader(),
                orderId = orderId
            )

            if (response.isSuccessful && response.body() != null) {
                val capture = response.body()!!
                Log.d(TAG, "Order captured successfully: ${capture.id}")
                Result.success(capture)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Order capture failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Order capture failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Order capture error", e)
            Result.failure(e)
        }
    }
}