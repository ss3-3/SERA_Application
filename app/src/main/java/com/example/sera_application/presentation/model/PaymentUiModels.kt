package com.example.sera_application.presentation.model

data class OrderData(
    val eventName: String,
    val orderId: String,
    val price: String,
    val tickets: String,
    val status: String,
    val date: String
)

data class ReceiptData(
    val eventName: String,
    val transactionId: String,
    val date: String,
    val time: String,
    val venue: String,
    val ticketType: String,
    val quantity: Int,
    val seats: String,
    val price: Double,
    val email: String,
    val name: String,
    val phone: String
)
