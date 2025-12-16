package com.example.sera_application.presentation.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.enums.ReservationStatus
import com.example.sera_application.domain.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//data class ReservationUiState(
//    val reservations: List<Reservation> = emptyList(),
//    val isLoading: Boolean = false,
//    val error: String? = null
//)
//
//@HiltViewModel
//class ReservationViewModel @Inject constructor(
//    private val reservationRepository: ReservationRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ReservationUiState())
//    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()
//
//    // retrieve user's all reservation
//    fun loadUserReservations(userId: String) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                reservationRepository.getReservationsByUserId(userId)
//                    .catch { e ->
//                        _uiState.update {
//                            it.copy(
//                                isLoading = false,
//                                error = e.message ?: "Unknown error"
//                            )
//                        }
//                    }
//                    .collect { reservations ->
//                        _uiState.update {
//                            it.copy(
//                                reservations = reservations,
//                                isLoading = false,
//                                error = null
//                            )
//                        }
//                    }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Unknown error"
//                    )
//                }
//            }
//        }
//    }
//
//    //  Retrieve all bookings for a particular event (for organizer)
//    fun loadEventReservations(eventId: String) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                reservationRepository.getReservationsByEventId(eventId)
//                    .catch { e ->
//                        _uiState.update {
//                            it.copy(
//                                isLoading = false,
//                                error = e.message ?: "Unknown error"
//                            )
//                        }
//                    }
//                    .collect { reservations ->
//                        _uiState.update {
//                            it.copy(
//                                reservations = reservations,
//                                isLoading = false,
//                                error = null
//                            )
//                        }
//                    }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        isLoading = false,
//                        error = e.message ?: "Unknown error"
//                    )
//                }
//            }
//        }
//    }
//
//    // get reservation detail
//    suspend fun getReservationById(reservationId: String): Reservation? {
//        return reservationRepository.getReservationById(reservationId)
//    }
//
//    // create reservation
//    fun createReservation(reservation: Reservation, onSuccess: () -> Unit, onError: (String) -> Unit) {
//        viewModelScope.launch {
//            try {
//                val success = reservationRepository.createReservation(reservation)
//                if (success) {
//                    onSuccess()
//                } else {
//                    onError("Failed to create reservation")
//                }
//            } catch (e: Exception) {
//                onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    // cancel reservation
//    fun cancelReservation(reservationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
//        viewModelScope.launch {
//            try {
//                val success = reservationRepository.cancelReservation(reservationId)
//                if (success) {
//                    onSuccess()
//                } else {
//                    onError("Failed to cancel reservation")
//                }
//            } catch (e: Exception) {
//                onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    // update reservation status
//    fun updateReservationStatus(
//        reservationId: String,
//        status: ReservationStatus,
//        onSuccess: () -> Unit,
//        onError: (String) -> Unit
//    ) {
//        viewModelScope.launch {
//            try {
//                val success = reservationRepository.updateReservationStatus(reservationId, status)
//                if (success) {
//                    onSuccess()
//                } else {
//                    onError("Failed to update reservation status")
//                }
//            } catch (e: Exception) {
//                onError(e.message ?: "Unknown error")
//            }
//        }
//    }
