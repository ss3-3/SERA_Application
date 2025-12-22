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
        android.util.Log.d("FirebasePaymentDataSource", "=== Querying payments for userId: '$userId' ===")
        return try {
            val snapshot = paymentsRef
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            android.util.Log.d("FirebasePaymentDataSource", "✅ Found ${snapshot.documents.size} payment documents for userId: '$userId'")
            
            // Log all document IDs and userIds for debugging
            snapshot.documents.forEachIndexed { index, doc ->
                val docUserId = doc.data?.get("userId")?.toString()
                android.util.Log.d("FirebasePaymentDataSource", "  Payment[$index]: docId=${doc.id}, userId in doc='$docUserId', matches query: ${docUserId == userId}")
            }
            
            val payments = snapshot.documents.mapNotNull { it.toPayment() }
            android.util.Log.d("FirebasePaymentDataSource", "✅ Successfully mapped ${payments.size} payments for userId: '$userId'")
            payments
        } catch (e: Exception) {
            android.util.Log.e("FirebasePaymentDataSource", "❌ Error querying payments for userId '$userId': ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getPaymentsByEvent(eventId: String): List<Payment> {
        android.util.Log.d("FirebasePaymentDataSource", "=== QUERYING PAYMENTS FROM FIREBASE ===")
        android.util.Log.d("FirebasePaymentDataSource", "Querying payments for eventId: '$eventId'")
        android.util.Log.d("FirebasePaymentDataSource", "EventId length: ${eventId.length}, isBlank: ${eventId.isBlank()}")

        if (eventId.isBlank()) {
            android.util.Log.w("FirebasePaymentDataSource", "EventId is blank, returning empty list")
            return emptyList()
        }

        return try {
            // Query by eventId only - security rules will verify organizerId via event document
            android.util.Log.d("FirebasePaymentDataSource", "Executing Firestore query: paymentsRef.whereEqualTo('eventId', '$eventId')")
            val snapshot = paymentsRef
                .whereEqualTo("eventId", eventId)
                .get()
                .await()

            android.util.Log.d("FirebasePaymentDataSource", "✅ Firestore query completed successfully")
            android.util.Log.d("FirebasePaymentDataSource", "Found ${snapshot.documents.size} payment documents for eventId: '$eventId'")
            
            // Log all document IDs found
            if (snapshot.documents.isNotEmpty()) {
                android.util.Log.d("FirebasePaymentDataSource", "Payment document IDs found:")
                snapshot.documents.forEachIndexed { index, doc ->
                    android.util.Log.d("FirebasePaymentDataSource", "  [$index] Document ID: ${doc.id}")
                    val docEventId = doc.data?.get("eventId")?.toString()
                    android.util.Log.d("FirebasePaymentDataSource", "      eventId in document: '$docEventId' (matches query: ${docEventId == eventId})")
                }
            }

            // Don't try to query all payments as fallback - this causes permission denied errors for organizers
            if (snapshot.documents.isEmpty()) {
                android.util.Log.w("FirebasePaymentDataSource", "No payments found with eventId=$eventId")
                android.util.Log.w("FirebasePaymentDataSource", "This could mean:")
                android.util.Log.w("FirebasePaymentDataSource", "1. No payments exist for this event")
                android.util.Log.w("FirebasePaymentDataSource", "2. Payments exist but eventId field doesn't match")
                android.util.Log.w("FirebasePaymentDataSource", "3. Firestore security rules may be blocking the query")
            }

            val payments = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    val statusFromFirestore = data?.get("status")?.toString()
                    android.util.Log.d("FirebasePaymentDataSource", "Processing payment doc ${doc.id}: data keys=${data?.keys}, eventId=${data?.get("eventId")}, status=${statusFromFirestore}")

                    val payment = doc.toPayment()
                    if (payment != null) {
                        android.util.Log.d("FirebasePaymentDataSource", "Mapped payment: ${payment.paymentId}, eventId=${payment.eventId}, userId=${payment.userId}, amount=${payment.amount}, status=${payment.status.name}")

                        // Log specifically for REFUND_PENDING payments
                        if (payment.status.name == "REFUND_PENDING") {
                            android.util.Log.d("FirebasePaymentDataSource", "*** REFUND_PENDING payment found: ${payment.paymentId}, eventId=${payment.eventId}, status from Firestore=$statusFromFirestore")
                        }

                        // Verify eventId matches
                        if (payment.eventId != eventId) {
                            android.util.Log.w("FirebasePaymentDataSource", "Payment ${payment.paymentId} has eventId=${payment.eventId} but query was for eventId=$eventId")
                        }
                    } else {
                        android.util.Log.w("FirebasePaymentDataSource", "Payment doc ${doc.id} could not be mapped to Payment object")
                    }
                    payment
                } catch (e: Exception) {
                    android.util.Log.e("FirebasePaymentDataSource", "Error mapping payment document ${doc.id}: ${e.message}", e)
                    e.printStackTrace()
                    null
                }
            }

            android.util.Log.d("FirebasePaymentDataSource", "✅ Successfully mapped ${payments.size} payments for eventId: '$eventId'")
            android.util.Log.d("FirebasePaymentDataSource", "=== END QUERY - Returning ${payments.size} payments ===")
            
            // Final verification - log all returned payment eventIds
            payments.forEachIndexed { index, payment ->
                android.util.Log.d("FirebasePaymentDataSource", "Returned Payment[$index]: paymentId=${payment.paymentId}, eventId='${payment.eventId}', status=${payment.status.name}")
                if (payment.eventId != eventId) {
                    android.util.Log.e("FirebasePaymentDataSource", "⚠️ WARNING: Payment ${payment.paymentId} has mismatched eventId! Expected: '$eventId', Got: '${payment.eventId}'")
                }
            }
            
            payments
        } catch (e: Exception) {
            android.util.Log.e("FirebasePaymentDataSource", "❌ ERROR querying payments for eventId '$eventId': ${e.message}", e)
            android.util.Log.e("FirebasePaymentDataSource", "Exception type: ${e.javaClass.simpleName}")
            
            // Check for specific Firestore errors
            if (e is com.google.firebase.firestore.FirebaseFirestoreException) {
                android.util.Log.e("FirebasePaymentDataSource", "Firestore Error Code: ${e.code}")
                when (e.code) {
                    com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        android.util.Log.e("FirebasePaymentDataSource", "🔒 PERMISSION DENIED: Organizer may not have access to payments. Check Firestore security rules.")
                        android.util.Log.e("FirebasePaymentDataSource", "   - Verify user role is 'ORGANIZER' in users collection")
                        android.util.Log.e("FirebasePaymentDataSource", "   - Verify event exists and organizerId matches current user")
                        android.util.Log.e("FirebasePaymentDataSource", "   - Verify payment document has correct eventId field")
                    }
                    com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND -> {
                        android.util.Log.e("FirebasePaymentDataSource", "📭 NOT FOUND: Event or payment document not found")
                    }
                    else -> {
                        android.util.Log.e("FirebasePaymentDataSource", "❓ Other Firestore error: ${e.code}")
                    }
                }
            } else if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                       e.message?.contains("permission", ignoreCase = true) == true) {
                android.util.Log.e("FirebasePaymentDataSource", "🔒 PERMISSION DENIED (from message): Organizer may not have access to payments collection. Check Firestore security rules.")
            }

            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updatePaymentStatus(paymentId: String, status: String) {
        paymentsRef.document(paymentId)
            .update("status", status)
            .await()
    }
}