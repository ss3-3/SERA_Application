package com.example.sera_application.domain.util

import javax.inject.Inject

class PriceCalculator @Inject constructor() {

    data class PriceBreakdown(
        val basePrice: Double,
        val serviceFee: Double,
        val tax: Double,
        val discount: Double,
        val totalPrice: Double
    )

    fun calculateTotalPrice(
        pricePerSeat: Double,
        quantity: Int,
        applyServiceFee: Boolean = true,
        applyTax: Boolean = true,
        discountPercentage: Double = 0.0
    ): PriceBreakdown {
        val basePrice = pricePerSeat * quantity
        
        val serviceFee = if (applyServiceFee) {
            basePrice * SERVICE_FEE_PERCENTAGE
        } else {
            0.0
        }
        
        val subtotal = basePrice + serviceFee
        
        val discount = if (discountPercentage > 0) {
            subtotal * (discountPercentage / 100.0)
        } else {
            0.0
        }
        
        val afterDiscount = subtotal - discount
        
        val tax = if (applyTax) {
            afterDiscount * TAX_PERCENTAGE
        } else {
            0.0
        }
        
        val totalPrice = afterDiscount + tax
        
        return PriceBreakdown(
            basePrice = roundToTwoDecimals(basePrice),
            serviceFee = roundToTwoDecimals(serviceFee),
            tax = roundToTwoDecimals(tax),
            discount = roundToTwoDecimals(discount),
            totalPrice = roundToTwoDecimals(totalPrice)
        )
    }

    fun calculateRefundAmount(
        totalPrice: Double,
        hoursUntilEvent: Long
    ): Double {
        val refundPercentage = when {
            hoursUntilEvent >= 72 -> 1.0  // 100% refund
            hoursUntilEvent >= 48 -> 0.75 // 75% refund
            hoursUntilEvent >= 24 -> 0.5  // 50% refund
            else -> 0.0                    // No refund
        }
        
        return roundToTwoDecimals(totalPrice * refundPercentage)
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return kotlin.math.round(value * 100) / 100
    }

    companion object {
        const val SERVICE_FEE_PERCENTAGE = 0.05  // 5%
        const val TAX_PERCENTAGE = 0.06           // 6%
    }
}
