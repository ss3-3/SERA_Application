package com.example.sera_application.domain.exception

sealed class ReservationException(message: String) : Exception(message) {
    class InvalidQuantity(message: String = "Invalid seat quantity") : ReservationException(message)
    class SeatNotAvailable(message: String = "Requested seats are not available") : ReservationException(message)
    class EventNotFound(message: String = "Event not found") : ReservationException(message)
    class EventClosed(message: String = "Event is closed for reservations") : ReservationException(message)
    class ReservationNotFound(message: String = "Reservation not found") : ReservationException(message)
    class CancellationNotAllowed(message: String = "Cancellation not allowed") : ReservationException(message)
    class InvalidPrice(message: String = "Invalid price calculation") : ReservationException(message)
    class InsufficientCapacity(message: String = "Event has insufficient capacity") : ReservationException(message)
    class DuplicateReservation(message: String = "Duplicate reservation detected") : ReservationException(message)
}
