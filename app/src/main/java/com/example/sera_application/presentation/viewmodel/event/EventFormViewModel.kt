package com.example.sera_application.presentation.viewmodel.event

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.model.enums.EventStatus
import com.example.sera_application.domain.repository.ImageRepository
import com.example.sera_application.domain.usecase.event.CreateEventUseCase
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.event.UpdateEventUseCase
import com.example.sera_application.domain.usecase.image.SaveImageUseCase
import com.example.sera_application.presentation.ui.event.EventFormData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

/**
 * UI State for Event Form Screen (Create/Edit)
 */
data class EventFormUiState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val createdEventId: String? = null,
    val imagePath: String? = null // For newly uploaded image
)

@HiltViewModel
class EventFormViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val imageRepository: ImageRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val event = getEventByIdUseCase(eventId)
                _uiState.update {
                    it.copy(
                        event = event,
                        isLoading = false,
                        errorMessage = if (event == null) "Event not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load event: ${e.message}") }
            }
        }
    }

    fun getEventFormData(): EventFormData? {
        val event = _uiState.value.event ?: return null
        return EventFormData(
            name = event.name,
            dateMillis = event.date,
            startTimeMillis = event.startTime,
            endTimeMillis = event.endTime,
            location = event.location,
            category = event.category.displayName,
            rockZoneSeats = event.rockZoneSeats.toString(),
            normalZoneSeats = event.normalZoneSeats.toString(),
            rockZonePrice = event.rockZonePrice,
            normalZonePrice = event.normalZonePrice,
            description = event.description,
            imagePath = event.imagePath
        )
    }

    fun submitEvent(
        formData: EventFormData,
        isEditMode: Boolean
    ) {
        if (isEditMode) {
            updateEvent(formData)
        } else {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.update { it.copy(errorMessage = "User not logged in") }
                return
            }
            createEvent(
                formData = formData,
                organizerId = currentUser.uid,
                organizerName = currentUser.displayName ?: "Organizer"
            )
        }
    }

    /**
     * Handles image selection from gallery picker.
     * Uploads the image to Firebase Storage and updates UI state with the download URL.
     * This ensures all users can see the event images.
     * 
     * @param uri The Uri of the selected image
     */
    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Generate unique file name for event image
                val fileName = "event_${UUID.randomUUID()}.jpg"
                
                // Upload image to Firebase Storage to make it accessible to all users
                val downloadUrl = imageRepository.uploadImageToFirebase(uri, "events", fileName)
                
                if (downloadUrl != null) {
                    _uiState.update { 
                        it.copy(
                            imagePath = downloadUrl, // Store Firebase Storage download URL
                            isLoading = false
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to upload image. Please try again."
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Image upload failed: ${e.message ?: "Unknown error"}"
                    ) 
                }
            }
        }
    }

    private fun createEvent(
        formData: EventFormData,
        organizerId: String,
        organizerName: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                val validationError = validateFormData(formData)
                if (validationError != null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = validationError) }
                    return@launch
                }

                val eventId = UUID.randomUUID().toString()
                val event = convertFormDataToEvent(
                    formData = formData,
                    eventId = eventId,
                    organizerId = organizerId,
                    organizerName = organizerName
                )

                val success = createEventUseCase(event)
                if (success) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, createdEventId = event.eventId) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to create event") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error creating event: ${e.message}") }
            }
        }
    }

    private fun updateEvent(formData: EventFormData) {
        viewModelScope.launch {
            val currentEvent = _uiState.value.event ?: run {
                _uiState.update { it.copy(errorMessage = "No event loaded for editing") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                val validationError = validateFormData(formData)
                if (validationError != null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = validationError) }
                    return@launch
                }

                val updatedEvent = convertFormDataToEvent(
                    formData = formData,
                    eventId = currentEvent.eventId,
                    organizerId = currentEvent.organizerId,
                    organizerName = currentEvent.organizerName,
                    createdAt = currentEvent.createdAt
                )

                val success = updateEventUseCase(updatedEvent)
                if (success) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, event = updatedEvent) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to update event") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error updating event: ${e.message}") }
            }
        }
    }

    private fun validateFormData(formData: EventFormData): String? {
        if (formData.name.isBlank()) return "Event name is required"
        if (formData.dateMillis == null) return "Please select an event date"
        if (formData.startTimeMillis == null) return "Please select a start time"
        if (formData.endTimeMillis == null) return "Please select an end time"

        if (formData.endTimeMillis <= formData.startTimeMillis) {
            return "End time must be after start time"
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (formData.dateMillis < today) {
            return "Event date cannot be in the past"
        }

        if (formData.location.isBlank()) return "Event location is required"
        if (formData.category.isBlank()) return "Event category is required"

        val rockSeats = formData.rockZoneSeats.toIntOrNull() ?: 0
        val normalSeats = formData.normalZoneSeats.toIntOrNull() ?: 0
        if (rockSeats + normalSeats <= 0) return "Total seats must be greater than 0"

        if (formData.rockZonePrice < 0) return "Rock zone price cannot be negative"
        if (formData.normalZonePrice < 0) return "Normal zone price cannot be negative"

        return null
    }

    private fun convertFormDataToEvent(
        formData: EventFormData,
        eventId: String,
        organizerId: String,
        organizerName: String,
        createdAt: Long = System.currentTimeMillis()
    ): Event {
        val rockZoneSeats = formData.rockZoneSeats.toIntOrNull() ?: 0
        val normalZoneSeats = formData.normalZoneSeats.toIntOrNull() ?: 0
        val totalSeats = rockZoneSeats + normalZoneSeats

        val category = EventCategory.values().find {
            it.displayName.equals(formData.category, ignoreCase = true)
        } ?: EventCategory.FESTIVAL

        val availableSeats = if (_uiState.value.event == null) {
            totalSeats
        } else {
            _uiState.value.event?.availableSeats ?: totalSeats
        }

        // Combine date from date picker with time from time picker
//        val startDate = Calendar.getInstance().apply { timeInMillis = formData.dateMillis!! }
//        val startTimeCal = Calendar.getInstance().apply { timeInMillis = formData.startTimeMillis!! }
//        val endTimeCal = Calendar.getInstance().apply { timeInMillis = formData.endTimeMillis!! }
//
//        val finalStartTime = Calendar.getInstance().apply {
//            set(Calendar.YEAR, startDate.get(Calendar.YEAR))
//            set(Calendar.MONTH, startDate.get(Calendar.MONTH))
//            set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH))
//            set(Calendar.HOUR_OF_DAY, startTimeCal.get(Calendar.HOUR_OF_DAY))
//            set(Calendar.MINUTE, startTimeCal.get(Calendar.MINUTE))
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }.timeInMillis
//
//        val finalEndTime = Calendar.getInstance().apply {
//            set(Calendar.YEAR, startDate.get(Calendar.YEAR))
//            set(Calendar.MONTH, startDate.get(Calendar.MONTH))
//            set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH))
//            set(Calendar.HOUR_OF_DAY, endTimeCal.get(Calendar.HOUR_OF_DAY))
//            set(Calendar.MINUTE, endTimeCal.get(Calendar.MINUTE))
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }.timeInMillis
        val finalStartTime = formData.startTimeMillis!!
        val finalEndTime = formData.endTimeMillis!!

        return Event(
            eventId = eventId,
            name = formData.name,
            organizerId = organizerId,
            organizerName = organizerName,
            description = formData.description,
            category = category,
            status = _uiState.value.event?.status ?: EventStatus.PENDING,
            date = finalStartTime, // Use the start time as the primary date reference
            startTime = finalStartTime,
            endTime = finalEndTime,
            location = formData.location,
            rockZoneSeats = rockZoneSeats,
            normalZoneSeats = normalZoneSeats,
            totalSeats = totalSeats,
            availableSeats = availableSeats,
            rockZonePrice = formData.rockZonePrice,
            normalZonePrice = formData.normalZonePrice,
            imagePath = _uiState.value.imagePath ?: formData.imagePath ?: _uiState.value.event?.imagePath,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun resetSuccessState() {
        _uiState.update {
            it.copy(
                isSuccess = false,
                createdEventId = null,
                imagePath = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
