package com.example.sera_application.data.api

import android.util.Base64
import android.util.Log

class PayPalRepository(
    private val clientId: String,
    private val clientSecret: String,
    private val isSandbox: Boolean = true
) {
    private val apiService = PayPalApiService.create(isSandbox)
    private var accessToken: String? = null
    private val TAG = "PayPalRepository"

    private fun getBasicAuthHeader(): String {
        val credentials = "$clientId:$clientSecret"
        val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    private fun getBearerAuthHeader(): String {
        return "Bearer $accessToken"
    }

    suspend fun authenticate(): Result<String> {
        return try {
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
                val errorMsg = "Authentication failed: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
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
    ): Result<PayPalOrderResponse> {
        return try {
            if (accessToken == null) {
                Log.d(TAG, "No access token, authenticating first...")
                authenticate().getOrNull() ?: return Result.failure(Exception("Failed to authenticate"))
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
                Log.d(TAG, "Order created successfully: ${order.id}, Status: ${order.status}")
                Result.success(order)
            } else {
                val errorMsg = "Order creation failed: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Order creation error", e)
            Result.failure(e)
        }
    }

    suspend fun captureOrder(orderId: String): Result<PayPalCaptureResponse> {
        return try {
            if (accessToken == null) {
                Log.d(TAG, "No access token, authenticating first...")
                authenticate().getOrNull() ?: return Result.failure(Exception("Failed to authenticate"))
            }

            Log.d(TAG, "Capturing order: $orderId")

            val response = apiService.captureOrder(
                bearerToken = getBearerAuthHeader(),
                orderId = orderId
            )

            if (response.isSuccessful && response.body() != null) {
                val capture = response.body()!!
                Log.d(TAG, "Order captured successfully: ${capture.id}, Status: ${capture.status}")
                Result.success(capture)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = "Order capture failed: ${response.code()} - ${response.message()} - $errorBody"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Order capture error", e)
            Result.failure(e)
        }
    }
}