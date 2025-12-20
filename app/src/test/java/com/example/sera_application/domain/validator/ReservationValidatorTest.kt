//package com.example.sera_application.domain.validator
//
//import com.example.sera_application.domain.model.EventReservation
//import com.example.sera_application.domain.model.ValidationResult
//import com.example.sera_application.domain.model.enums.ReservationStatus
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Test
//
//class ReservationValidatorTest {
//
//    private lateinit var validator: ReservationValidator
//
//    @Before
//    fun setup() {
//        validator = ReservationValidator()
//    }
//
//    @Test
//    fun `validate valid reservation returns success`() {
//        val reservation = createValidReservation()
//        val result = validator.validateReservation(reservation)
//        assertTrue(result is ValidationResult.Success)
//    }
//
//    @Test
//    fun `validate reservation with zero quantity returns error`() {
//        val reservation = createValidReservation().copy(quantity = 0)
//        val result = validator.validateReservation(reservation)
//        assertTrue(result is ValidationResult.MultipleErrors)
//    }
//
//    @Test
//    fun `validate reservation with negative price returns error`() {
//        val reservation = createValidReservation().copy(pricePerSeat = -10.0)
//        val result = validator.validateReservation(reservation)
//        assertTrue(result is ValidationResult.MultipleErrors)
//    }
//
//    @Test
//    fun `validate reservation with price mismatch returns error`() {
//        val reservation = createValidReservation().copy(
//            pricePerSeat = 100.0,
//            quantity = 2,
//            totalPrice = 150.0 // Should be 200.0
//        )
//        val result = validator.validateReservation(reservation)
//        assertTrue(result is ValidationResult.MultipleErrors)
//    }
//
//    @Test
//    fun `validate reservation with empty event ID returns error`() {
//        val reservation = createValidReservation().copy(eventId = "")
//        val result = validator.validateReservation(reservation)
//        assertTrue(result is ValidationResult.MultipleErrors)
//    }
//
//    @Test
//    fun `validate reservation with too many seats returns error`() {
//        val reservation = createValidReservation().copy(quantity = 15)
//        val result = validator.validateReservation(reservation)
//        assertTrue(result is ValidationResult.MultipleErrors)
//    }
//
//    @Test
//    fun `validate cancellation of confirmed reservation within deadline returns success`() {
//        val currentTime = System.currentTimeMillis()
//        val reservation = createValidReservation().copy(
//            status = ReservationStatus.CONFIRMED,
//            createdAt = currentTime - (12 * 60 * 60 * 1000) // 12 hours ago
//        )
//        val result = validator.validateCancellation(reservation, currentTime)
//        assertTrue(result is ValidationResult.Success)
//    }
//
//    @Test
//    fun `validate cancellation of already cancelled reservation returns error`() {
//        val currentTime = System.currentTimeMillis()
//        val reservation = createValidReservation().copy(
//            status = ReservationStatus.CANCELLED
//        )
//        val result = validator.validateCancellation(reservation, currentTime)
//        assertTrue(result is ValidationResult.MultipleErrors)
//    }
//
//    @Test
//    fun `validate seat numbers with correct quantity returns success`() {
//        val result = validator.validateSeatNumbers("A1, A2, A3", 3)
//        assertTrue(result is ValidationResult.Success)
//    }
//
//    @Test
//    fun `validate seat numbers with quantity mismatch returns error`() {
//        val result = validator.validateSeatNumbers("A1, A2", 3)
//        assertTrue(result is ValidationResult.Error)
//    }
//
//    @Test
//    fun `validate seat numbers with duplicates returns error`() {
//        val result = validator.validateSeatNumbers("A1, A2, A1", 3)
//        assertTrue(result is ValidationResult.Error)
//    }
//
//    private fun createValidReservation() = EventReservation(
//        reservationId = "RES-001",
//        eventId = "EVENT-001",
//        userId = "USER-001",
//        zoneId = "ZONE-A",
//        zoneName = "VIP",
//        quantity = 2,
//        seatNumbers = "A1, A2",
//        pricePerSeat = 100.0,
//        totalPrice = 200.0,
//        status = ReservationStatus.CONFIRMED,
//        createdAt = System.currentTimeMillis()
//    )
//}
