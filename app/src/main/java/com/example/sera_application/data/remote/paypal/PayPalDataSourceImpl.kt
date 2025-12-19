package com.example.sera_application.data.remote.paypal
//
//import com.example.sera_application.data.remote.datasource.PayPalRemoteDataSource
//import com.example.sera_application.data.remote.paypal.api.PayPalBackendApi
//import com.example.sera_application.domain.model.PayPalOrderResult
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import javax.inject.Inject
//
///**
// * Stub implementation of [PayPalRemoteDataSource].
// *
// * This class is intentionally simple and **does not** perform real
// * network calls to PayPal or your backend yet. It is designed to
// * compile safely while providing a clean abstraction that can later
// * delegate to a real backend implementation via [PayPalBackendApi].
// */
//class PayPalDataSourceImpl @Inject constructor(
//    private val backendApi: PayPalBackendApi
//) : PayPalRemoteDataSource {
//
//    override suspend fun createOrder(
//        amount: Double,
//        currency: String
//    ): PayPalOrderResult = withContext(Dispatchers.IO) {
//        // Basic client-side validation so we don't even try to talk
//        // to the backend with obviously invalid data.
//        if (amount <= 0.0) {
//            return@withContext PayPalOrderResult.Failed(
//                errorMessage = "Amount must be greater than zero."
//            )
//        }
//
//        if (currency.isBlank()) {
//            return@withContext PayPalOrderResult.Failed(
//                errorMessage = "Currency must not be blank."
//            )
//        }
//
//        // TODO: Implement real backend call using backendApi.createOrder(...)
//        // The backend should:
//        //  - Handle OAuth2 with PayPal
//        //  - Create a PayPal order
//        //  - Return a stable order ID for the app to track
//        //
//        // For now, we return a deterministic but obviously fake order ID.
//        val fakeOrderId = "ORDER-${System.currentTimeMillis()}"
//
//        PayPalOrderResult.Success(orderId = fakeOrderId)
//    }
//
//    override suspend fun captureOrder(
//        orderId: String
//    ): PayPalOrderResult = withContext(Dispatchers.IO) {
//        if (orderId.isBlank()) {
//            return@withContext PayPalOrderResult.Failed(
//                errorMessage = "Order ID must not be blank."
//            )
//        }
//
//        // TODO: Implement real backend call using backendApi.captureOrder(...)
//        // The backend should:
//        //  - Capture the PayPal order
//        //  - Confirm final payment status
//        //
//        // For now we optimistically assume capture succeeds.
//        PayPalOrderResult.Success(orderId = orderId)
//    }
//}
