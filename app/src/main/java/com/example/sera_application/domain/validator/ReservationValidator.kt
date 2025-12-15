package com.example.sera_application.domain.validator

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.ValidationError
import com.example.sera_application.domain.model.ValidationResult
import com.example.sera_application.domain.model.enums.ReservationStatus
import javax.inject.Inject

class ReservationValidator @Inject constructor() {

    fun validateReservation(reservation: EventReservation): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate quantity
        if (reservation.quantity <= 0) {
            errors.add(ValidationError("quantity", "Quantity must be greater than 0"))
        }

        if (reservation.quantity > MAX_SEATS_PER_RESERVATION) {
            errors.add(ValidationError("quantity", "Cannot reserve more than $MAX_SEATS_PER_RESERVATION seats at once"))
        }

        // Validate price
        if (reservation.pricePerSeat < 0) {
            errors.add(ValidationError("pricePerSeat", "Price per seat cannot be negative"))
        }

        if (reservation.totalPrice < 0) {
            errors.add(ValidationError("totalPrice", "Total price cannot be negative"))
        }

        // Validate price calculation
        val expectedTotal = reservation.pricePerSeat * reservation.quantity
        if (reservation.totalPrice != expectedTotal) {
            errors.add(ValidationError("totalPrice", "Total price mismatch. Expected: $expectedTotal"))
        }

        // Validate IDs
        if (reservation.eventId.isBlank()) {
            errors.add(ValidationError("eventId", "Event ID cannot be empty"))
        }

        if (reservation.userId.isBlank()) {
            errors.add(ValidationError("userId", "User ID cannot be empty"))
        }

        if (reservation.zoneId.isBlank()) {
            errors.add(ValidationError("zoneId", "Zone ID cannot be empty"))
        }

        // Validate seat numbers
        if (reservation.seatNumbers.isBlank()) {
            errors.add(ValidationError("seatNumbers", "Seat numbers cannot be empty"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.MultipleErrors(errors)
        }
    }

    fun validateCancellation(
        reservation: EventReservation,
        currentTimeMillis: Long
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check if already cancelled
        if (reservation.status == ReservationStatus.CANCELLED) {
            errors.add(ValidationError("status", "Reservation is already cancelled"))
        }

        // Check if completed
        if (reservation.status == ReservationStatus.COMPLETED) {
            errors.add(ValidationError("status", "Cannot cancel completed reservation"))
        }

        // Check cancellation deadline (e.g., 24 hours before event)
        val cancellationDeadline = reservation.createdAt + CANCELLATION_DEADLINE_HOURS * 60 * 60 * 1000
        if (currentTimeMillis > cancellationDeadline) {
            errors.add(ValidationError("time", "Cancellation deadline has passed"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.MultipleErrors(errors)
        }
    }

    fun validateSeatNumbers(seatNumbers: String, expectedQuantity: Int): ValidationResult {
        if (seatNumbers.isBlank()) {
            return ValidationResult.Error("Seat numbers cannot be empty", "seatNumbers")
        }

        val seats = seatNumbers.split(",").map { it.trim() }
        
        if (seats.size != expectedQuantity) {
            return ValidationResult.Error(
                "Number of seats (${seats.size}) does not match quantity ($expectedQuantity)",
                "seatNumbers"
            )
        }

        // Check for duplicates
        if (seats.size != seats.distinct().size) {
            return ValidationResult.Error("Duplicate seat numbers found", "seatNumbers")
        }

        return ValidationResult.Success
    }

    companion object {
        const val MAX_SEATS_PER_RESERVATION = 10
        const val CANCELLATION_DEADLINE_HOURS = 24
    }
}
