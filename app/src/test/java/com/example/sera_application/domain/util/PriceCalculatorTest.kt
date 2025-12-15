package com.example.sera_application.domain.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PriceCalculatorTest {

    private lateinit var calculator: PriceCalculator

    @Before
    fun setup() {
        calculator = PriceCalculator()
    }

    @Test
    fun `calculate total price with all fees`() {
        val breakdown = calculator.calculateTotalPrice(
            pricePerSeat = 100.0,
            quantity = 2,
            applyServiceFee = true,
            applyTax = true
        )

        assertEquals(200.0, breakdown.basePrice, 0.01)
        assertEquals(10.0, breakdown.serviceFee, 0.01) // 5% of 200
        assertEquals(12.6, breakdown.tax, 0.01) // 6% of 210
        assertEquals(0.0, breakdown.discount, 0.01)
        assertEquals(222.6, breakdown.totalPrice, 0.01)
    }

    @Test
    fun `calculate total price without service fee`() {
        val breakdown = calculator.calculateTotalPrice(
            pricePerSeat = 100.0,
            quantity = 2,
            applyServiceFee = false,
            applyTax = true
        )

        assertEquals(200.0, breakdown.basePrice, 0.01)
        assertEquals(0.0, breakdown.serviceFee, 0.01)
        assertEquals(12.0, breakdown.tax, 0.01) // 6% of 200
        assertEquals(212.0, breakdown.totalPrice, 0.01)
    }

    @Test
    fun `calculate total price with discount`() {
        val breakdown = calculator.calculateTotalPrice(
            pricePerSeat = 100.0,
            quantity = 2,
            applyServiceFee = true,
            applyTax = true,
            discountPercentage = 10.0
        )

        assertEquals(200.0, breakdown.basePrice, 0.01)
        assertEquals(10.0, breakdown.serviceFee, 0.01)
        assertEquals(21.0, breakdown.discount, 0.01) // 10% of 210
        assertEquals(11.34, breakdown.tax, 0.01) // 6% of 189
        assertEquals(200.34, breakdown.totalPrice, 0.01)
    }

    @Test
    fun `calculate refund amount 72 hours before event`() {
        val refund = calculator.calculateRefundAmount(
            totalPrice = 200.0,
            hoursUntilEvent = 72
        )

        assertEquals(200.0, refund, 0.01) // 100% refund
    }

    @Test
    fun `calculate refund amount 48 hours before event`() {
        val refund = calculator.calculateRefundAmount(
            totalPrice = 200.0,
            hoursUntilEvent = 48
        )

        assertEquals(150.0, refund, 0.01) // 75% refund
    }

    @Test
    fun `calculate refund amount 24 hours before event`() {
        val refund = calculator.calculateRefundAmount(
            totalPrice = 200.0,
            hoursUntilEvent = 24
        )

        assertEquals(100.0, refund, 0.01) // 50% refund
    }

    @Test
    fun `calculate refund amount less than 24 hours before event`() {
        val refund = calculator.calculateRefundAmount(
            totalPrice = 200.0,
            hoursUntilEvent = 12
        )

        assertEquals(0.0, refund, 0.01) // No refund
    }

    @Test
    fun `calculate price for single seat`() {
        val breakdown = calculator.calculateTotalPrice(
            pricePerSeat = 50.0,
            quantity = 1,
            applyServiceFee = true,
            applyTax = true
        )

        assertEquals(50.0, breakdown.basePrice, 0.01)
        assertTrue(breakdown.totalPrice > 50.0)
    }
}
