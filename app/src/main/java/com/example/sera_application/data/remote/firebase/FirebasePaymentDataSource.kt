package com.example.sera_application.data.remote.firebase

import com.example.sera_application.data.mapper.PaymentFirestoreMapper
import com.example.sera_application.data.mapper.PaymentFirestoreMapper.toPayment
import com.example.sera_application.data.remote.datasource.PaymentRemoteDataSource
import com.example.sera_application.domain.model.Payment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePaymentDataSource(
    private val firestore: FirebaseFirestore
) : PaymentRemoteDataSource {

    private val paymentsRef = firestore.collection("payments")

    override suspend fun savePayment(payment: Payment): String {
        val docRef = if (payment.paymentId.isBlank()) {
            paymentsRef.document()
        } else {
            paymentsRef.document(payment.paymentId)
        }
        val paymentWithId = payment.copy(paymentId = docRef.id)
        val paymentMap = PaymentFirestoreMapper.paymentToFirestoreMap(paymentWithId)
        docRef.set(paymentMap).await()
        return docRef.id
    }

    override suspend fun getPaymentByReservation(reservationId: String): Payment? {
        return paymentsRef
            .whereEqualTo("reservationId", reservationId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toPayment()
    }

    override suspend fun getPaymentById(paymentId: String): Payment? {
        val document = paymentsRef.document(paymentId).get().await()
        return if (document.exists()) {
            document.toPayment()
        } else {
            null
        }
    }

    override suspend fun getPaymentsByUser(userId: String): List<Payment> {
        val snapshot = paymentsRef
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toPayment() }
    }

    override suspend fun updatePaymentStatus(paymentId: String, status: String) {
        paymentsRef.document(paymentId)
            .update("status", status)
            .await()
    }
}
