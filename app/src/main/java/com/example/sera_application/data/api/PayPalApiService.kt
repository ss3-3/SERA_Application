package com.example.sera_application.data.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class PayPalAccessTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class PayPalOrderRequest(
    val intent: String = "CAPTURE",
    @SerializedName("purchase_units") val purchaseUnits: List<PurchaseUnit>,
    @SerializedName("application_context") val applicationContext: ApplicationContext? = null
)

data class PurchaseUnit(
    val amount: Amount,
    val description: String? = null
)

data class Amount(
    @SerializedName("currency_code") val currencyCode: String,
    val value: String
)

data class ApplicationContext(
    @SerializedName("return_url") val returnUrl: String,
    @SerializedName("cancel_url") val cancelUrl: String
)

data class PayPalOrderResponse(
    val id: String,
    val status: String,
    val links: List<Link>
)

data class Link(
    val href: String,
    val rel: String,
    val method: String
)

data class PayPalCaptureResponse(
    val id: String,
    val status: String,
    @SerializedName("purchase_units") val purchaseUnits: List<PurchaseUnitCapture>?
)

data class PurchaseUnitCapture(
    val payments: Payments?
)

data class Payments(
    val captures: List<Capture>?
)

data class Capture(
    val id: String,
    val status: String,
    val amount: Amount
)

interface PayPalApiService {

    @FormUrlEncoded
    @POST("v1/oauth2/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<PayPalAccessTokenResponse>

    @POST("v2/checkout/orders")
    suspend fun createOrder(
        @Header("Authorization") bearerToken: String,
        @Body orderRequest: PayPalOrderRequest
    ): Response<PayPalOrderResponse>

    @Headers("Content-Type: application/json")
    @POST("v2/checkout/orders/{order_id}/capture")
    suspend fun captureOrder(
        @Header("Authorization") bearerToken: String,
        @Path("order_id") orderId: String
    ): Response<PayPalCaptureResponse>

    companion object {
        private const val SANDBOX_BASE_URL = "https://api-m.sandbox.paypal.com/"
        private const val PRODUCTION_BASE_URL = "https://api-m.paypal.com/"

        fun create(isSandbox: Boolean = true): PayPalApiService {
            // Logging Interceptor
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val headerInterceptor = okhttp3.Interceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(headerInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(if (isSandbox) SANDBOX_BASE_URL else PRODUCTION_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(PayPalApiService::class.java)
        }
    }
}