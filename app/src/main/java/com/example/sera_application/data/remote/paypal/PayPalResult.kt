package com.example.sera_application.data.remote.paypal
//
///**
// * Result of creating a PayPal order.
// */
//sealed class PayPalOrderResult {
//    /**
//     * Order created successfully.
//     * @param orderId PayPal order ID
//     * @param approvalUrl URL to redirect user for payment approval
//     */
//    data class Success(
//        val orderId: String,
//        val approvalUrl: String
//    ) : PayPalOrderResult()
//
//    /**
//     * Order creation failed.
//     * @param error Error message
//     * @param cause Optional exception cause
//     */
//    data class Error(
//        val error: String,
//        val cause: Throwable? = null
//    ) : PayPalOrderResult()
//}
//
///**
// * Result of capturing a PayPal order.
// */
//sealed class PayPalCaptureResult {
//    /**
//     * Order captured successfully (payment completed).
//     * @param captureId PayPal capture ID
//     * @param status Payment status (typically "COMPLETED")
//     * @param amount Amount captured
//     * @param currency Currency code
//     */
//    data class Success(
//        val captureId: String,
//        val status: String,
//        val amount: Double,
//        val currency: String
//    ) : PayPalCaptureResult()
//
//    /**
//     * Order capture failed.
//     * @param error Error message
//     * @param cause Optional exception cause
//     */
//    data class Error(
//        val error: String,
//        val cause: Throwable? = null
//    ) : PayPalCaptureResult()
//}
