package com.example.sera_application.domain.model

/**
 * Domain-level result for PayPal order operations (create/capture).
 *
 * This is intentionally backend-agnostic and does not expose any
 * PayPal-specific SDK or Android UI types so that the data layer
 * can evolve independently of the presentation layer.
 */
sealed class PayPalOrderResult {

    /**
     * Operation completed successfully.
     *
     * @param orderId Identifier of the PayPal order managed by the backend.
     */
    data class Success(val orderId: String) : PayPalOrderResult()

    /**
     * User cancelled the PayPal flow before completion.
     */
    object Cancelled : PayPalOrderResult()

    /**
     * Operation failed due to a validation, network, or backend error.
     *
     * @param errorMessage Human-readable error description, safe to show in UI.
     */
    data class Failed(val errorMessage: String) : PayPalOrderResult()
}


