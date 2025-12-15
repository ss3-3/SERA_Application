package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.util.PriceCalculator
import javax.inject.Inject

class CalculateReservationPriceUseCase @Inject constructor(
    private val priceCalculator: PriceCalculator
) {
    operator fun invoke(
        pricePerSeat: Double,
        quantity: Int,
        discountPercentage: Double = 0.0
    ): Result<PriceCalculator.PriceBreakdown> {
        return try {
            if (pricePerSeat < 0) {
                return Result.failure(Exception("Price per seat cannot be negative"))
            }
            if (quantity <= 0) {
                return Result.failure(Exception("Quantity must be greater than 0"))
            }
            if (discountPercentage < 0 || discountPercentage > 100) {
                return Result.failure(Exception("Discount percentage must be between 0 and 100"))
            }

            val breakdown = priceCalculator.calculateTotalPrice(
                pricePerSeat = pricePerSeat,
                quantity = quantity,
                discountPercentage = discountPercentage
            )
            
            Result.success(breakdown)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
