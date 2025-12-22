package com.example.sera_application.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Resend Email API Service
 * Sends emails directly via Resend API
 */
interface EmailService {
    @POST("emails")
    suspend fun sendEmail(
        @Header("Authorization") apiKey: String,
        @Body emailRequest: EmailRequest
    ): Response<EmailResponse>
}

/**
 * Request model for Resend API
 */
data class EmailRequest(
    val from: String,
    val to: String,
    val subject: String,
    val html: String
)

/**
 * Response model from Resend API
 */
data class EmailResponse(
    val id: String
)

