package com.example.sera_application.domain.usecase.reservation

import com.example.sera_application.domain.model.EventReservation
import com.example.sera_application.domain.model.ValidationResult
import com.example.sera_application.domain.validator.ReservationValidator
import javax.inject.Inject

class ValidateReservationUseCase @Inject constructor(
    private val validator: ReservationValidator
) {
    operator fun invoke(reservation: EventReservation): Result<Unit> {
        return when (val validationResult = validator.validateReservation(reservation)) {
            is ValidationResult.Success -> Result.success(Unit)
            is ValidationResult.Error -> Result.failure(Exception(validationResult.message))
            is ValidationResult.MultipleErrors -> {
                val errorMessage = validationResult.errors.joinToString("\n") { 
                    "${it.field}: ${it.message}" 
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }
}
