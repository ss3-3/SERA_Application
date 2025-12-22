package com.example.sera_application.domain.usecase.payment

import com.example.sera_application.domain.model.Payment
import com.example.sera_application.domain.repository.PaymentRepository
import javax.inject.Inject

class GetPaymentsByEventUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(eventId: String): List<Payment> {
        if (eventId.isBlank()) {
            android.util.Log.w("GetPaymentsByEventUseCase", "EventId is blank")
            return emptyList()
        }

        return try {
            android.util.Log.d("GetPaymentsByEventUseCase", "=== Fetching payments for eventId: $eventId ===")
            val payments = paymentRepository.getPaymentsByEvent(eventId)
            android.util.Log.d("GetPaymentsByEventUseCase", "✅ Fetched ${payments.size} payments for eventId: $eventId")
            
            if (payments.isEmpty()) {
                android.util.Log.w("GetPaymentsByEventUseCase", "⚠️ No payments found for eventId: $eventId")
                android.util.Log.w("GetPaymentsByEventUseCase", "   This could mean:")
                android.util.Log.w("GetPaymentsByEventUseCase", "   1. No payments exist for this event")
                android.util.Log.w("GetPaymentsByEventUseCase", "   2. Firestore security rules are blocking the query")
                android.util.Log.w("GetPaymentsByEventUseCase", "   3. Payment documents have incorrect eventId field")
            }
            
            payments
        } catch (e: Exception) {
            android.util.Log.e("GetPaymentsByEventUseCase", "❌ Error fetching payments for eventId $eventId: ${e.message}", e)
            
            // Check for specific Firestore errors
            if (e is com.google.firebase.firestore.FirebaseFirestoreException) {
                android.util.Log.e("GetPaymentsByEventUseCase", "Firestore Error Code: ${e.code}")
                when (e.code) {
                    com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        android.util.Log.e("GetPaymentsByEventUseCase", "🔒 PERMISSION DENIED: Organizer cannot access payments")
                        android.util.Log.e("GetPaymentsByEventUseCase", "   Check Firestore security rules for organizer access")
                    }
                    else -> {
                        android.util.Log.e("GetPaymentsByEventUseCase", "❓ Other Firestore error: ${e.code}")
                    }
                }
            }
            
            e.printStackTrace()
            emptyList()
        }
    }
}

