package com.example.sera_application.di

import com.example.sera_application.data.remote.datasource.AuthRemoteDataSource
import com.example.sera_application.data.remote.datasource.EventRemoteDataSource
import com.example.sera_application.data.remote.datasource.NotificationRemoteDataSource
import com.example.sera_application.data.remote.datasource.PayPalRemoteDataSource
import com.example.sera_application.data.remote.datasource.PaymentRemoteDataSource
//import com.example.sera_application.data.remote.datasource.PayPalRemoteDataSource
import com.example.sera_application.data.remote.datasource.ReservationRemoteDataSource
import com.example.sera_application.data.remote.datasource.UserRemoteDataSource
import com.example.sera_application.data.remote.firebase.FirebaseAuthDataSource
import com.example.sera_application.data.remote.firebase.FirebaseEventDataSource
import com.example.sera_application.data.remote.firebase.FirebaseNotificationDataSource
import com.example.sera_application.data.remote.firebase.FirebasePaymentDataSource
import com.example.sera_application.data.remote.firebase.FirebaseReservationDataSource
import com.example.sera_application.data.remote.firebase.FirebaseUserDataSource
import com.example.sera_application.data.remote.paypal.PayPalDataSourceImpl
import com.example.sera_application.data.remote.paypal.api.PayPalBackendApi
import com.example.sera_application.data.remote.paypal.repository.PayPalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
//import com.example.sera_application.data.remote.paypal.PayPalDataSourceImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network module for dependency injection.
 * Provides Firebase instances, Retrofit, and remote datasources.
 *
 * Note: Replace BASE_URL with your actual backend server URL.
 * Backend should handle PayPal OAuth2 and API communication.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    companion object {
        /**
         * Base URL for backend API.
         * TODO: Replace with actual backend URL (e.g., "https://api.yourbackend.com/")
         */
        private const val BASE_URL = "https://your-backend-api.com/"

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            val firestore = FirebaseFirestore.getInstance()
            // Disable offline persistence to always fetch fresh data
            // This prevents stale cached data from being displayed
            firestore.firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            return firestore
        }

        @Provides
        @Singleton
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }

        @Provides
        @Singleton
        fun provideGson(): Gson {
            return GsonBuilder()
                .setLenient()
                .create()
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Use NONE in production
            }

            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(
            gson: Gson,
            okHttpClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        @Provides
        @Singleton
        fun providePayPalBackendApi(retrofit: Retrofit): PayPalBackendApi {
            return retrofit.create(PayPalBackendApi::class.java)
        }

        @Provides
        @Singleton
        fun providePayPalRepository(): PayPalRepository {
            // TODO: Move these credentials to BuildConfig or a secure configuration
            // For now, using sandbox credentials
            return PayPalRepository(
                clientId = "AQTPtN2werWX-j1tUfqQwifM0cfqviYHUVl9exM5fj4Ac2-kYXpqyjuaWw9mya3Tiwe2ppXGYyHNcBAP",
                clientSecret = "EN02FWz7AC_SwRw6FuprITB4AT_XdM2ZMV2p1VSaBY7TJr-gONuIupplRCxQURSxBrMcPDmjxeUDfQf9",
                isSandbox = true
            )
        }

        // Firebase Remote Datasources
        @Provides
        @Singleton
        fun provideFirebaseAuthDataSource(
            firebaseAuth: FirebaseAuth
        ): AuthRemoteDataSource {
            return FirebaseAuthDataSource(firebaseAuth)
        }

        @Provides
        @Singleton
        fun provideFirebaseUserDataSource(
            firestore: FirebaseFirestore
        ): UserRemoteDataSource {
            return FirebaseUserDataSource(firestore)
        }

        @Provides
        @Singleton
        fun provideFirebaseEventDataSource(
            firestore: FirebaseFirestore
        ): EventRemoteDataSource {
            return FirebaseEventDataSource(firestore)
        }

        @Provides
        @Singleton
        fun provideFirebaseReservationDataSource(
            firestore: FirebaseFirestore
        ): ReservationRemoteDataSource {
            return FirebaseReservationDataSource(firestore)
        }

        @Provides
        @Singleton
        fun provideFirebasePaymentDataSource(
            firestore: FirebaseFirestore
        ): PaymentRemoteDataSource {
            return FirebasePaymentDataSource(firestore)
        }

        @Provides
        @Singleton
        fun provideFirebaseNotificationDataSource(
            firestore: FirebaseFirestore
        ): NotificationRemoteDataSource {
            return FirebaseNotificationDataSource(firestore)
        }
    }

    // PayPal Remote Datasource binding
    @Binds
    @Singleton
    abstract fun bindPayPalDataSource(
        payPalRemoteDataSourceImpl: PayPalDataSourceImpl
    ): PayPalRemoteDataSource
}