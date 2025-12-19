package com.example.sera_application.presentation.viewmodel.event

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.data.local.image.LocalImageManager
import com.example.sera_application.domain.model.Event
import com.example.sera_application.domain.model.enums.EventCategory
import com.example.sera_application.domain.usecase.event.CreateEventUseCase
import com.example.sera_application.domain.usecase.event.GetEventByIdUseCase
import com.example.sera_application.domain.usecase.event.UpdateEventUseCase
import com.example.sera_application.presentation.ui.event.EventFormData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

/**
 * UI State for Event Form Screen (Create/Edit)
 */
data class EventFormUiState(
    val event: Event? = null,
    val imagePath: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val createdEventId: String? = null
)

/**
 * Combined ViewModel for Create and Edit Event Screen
 * Handles both create and edit modes similar to EventFormScreen UI
 */
@HiltViewModel
class EventFormViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val localImageManager: LocalImageManager
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    /**
     * Load event for editing
     * Call this when in edit mode (eventId is not null)
     */
    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val event = getEventByIdUseCase(eventId)
                _uiState.update {
                    it.copy(
                        event = event,
                        imagePath = event?.imagePath,
                        isLoading = false,
                        errorMessage = if (event == null) "Event not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load event: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Convert Event to EventFormData for form pre-filling
     * Use this in edit mode to populate the form
     */
    fun getEventFormData(): EventFormData? {
        val event = _uiState.value.event ?: return null
        
        return EventFormData(
            name = event.name,
            date = event.date,
            duration = event.duration,
            time = event.timeRange,
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

    /**
     * Submit form data (handles both create and edit)
     * @param formData The form data to submit
     * @param isEditMode True if editing existing event, false if creating new event
     * @param organizerId Required for create mode
     * @param organizerName Required for create mode
     */
    fun submitEvent(
        formData: EventFormData,
        isEditMode: Boolean
    ) {
        if (isEditMode) {
            updateEvent(formData)
        } else {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.update {
                    it.copy(errorMessage = "User not logged in")
                }
                return
            }

            createEvent(
                formData = formData,
                organizerId = currentUser.uid,
                organizerName = currentUser.displayName ?: "Organizer"
            )
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val path = localImageManager.saveEventImage(uri)

                _uiState.update {
                    it.copy(
                        imagePath = path,
                        event = it.event?.copy(imagePath = path)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Image upload failed")
                }
            }
        }
    }


//    fun onImageSelected(uri: Uri) {
//        viewModelScope.launch {
//            try {
//                val path = localImageManager.saveEventImage(uri)
//                _uiState.update {
//                    it.copy(
//                        event = it.event?.copy(imagePath = path)
//                            ?: Event(
//                                eventId = "",
//                                name = "",
//                                organizerId = "",
//                                organizerName = "",
//                                description = "",
//                                category = EventCategory.FESTIVAL,
//                                status = com.example.sera_application.domain.model.enums.EventStatus.PENDING,
//                                date = "",
//                                startTime = "",
//                                endTime = "",
//                                duration = "",
//                                location = "",
//                                rockZoneSeats = 0,
//                                normalZoneSeats = 0,
//                                totalSeats = 0,
//                                availableSeats = 0,
//                                rockZonePrice = 0.0,
//                                normalZonePrice = 0.0,
//                                imagePath = path,
//                                createdAt = System.currentTimeMillis(),
//                                updatedAt = System.currentTimeMillis()
//                            )
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(errorMessage = "Image upload failed")
//                }
//            }
//        }
//    }

    /**
     * Create a new event from form data
     */
    private fun createEvent(
        formData: EventFormData,
        organizerId: String,
        organizerName: String
    ) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, errorMessage = null, isSuccess = false) 
            }
            
            try {
                // Validate form data
                val validationError = validateFormData(formData)
                if (validationError != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = validationError
                        )
                    }
                    return@launch
                }

                // Convert EventFormData to Event domain model
                val eventId = UUID.randomUUID().toString()
                val event = convertFormDataToEvent(
                    formData = formData,
                    eventId = eventId,
                    organizerId = organizerId,
                    organizerName = organizerName
                )
                
                // Create event
                val success = createEventUseCase(event)
                
                if (success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            createdEventId = event.eventId,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to create event"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error creating event: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Update event from form data
     */
    private fun updateEvent(formData: EventFormData) {
        viewModelScope.launch {
            val currentEvent = _uiState.value.event ?: run {
                _uiState.update {
                    it.copy(errorMessage = "No event loaded for editing")
                }
                return@launch
            }
            
            _uiState.update { 
                it.copy(isLoading = true, errorMessage = null, isSuccess = false) 
            }
            
            try {
                // Validate form data
                val validationError = validateFormData(formData)
                if (validationError != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = validationError
                        )
                    }
                    return@launch
                }

                // Convert EventFormData to Event domain model
                val updatedEvent = convertFormDataToEvent(
                    formData, 
                    currentEvent.eventId,
                    currentEvent.organizerId,
                    currentEvent.organizerName,
                    currentEvent.createdAt
                )
                
                // Update event
                val success = updateEventUseCase(updatedEvent)
                
                if (success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            event = updatedEvent,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to update event"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error updating event: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Validate form data
     */
    private fun validateFormData(formData: EventFormData): String? {
        if (formData.name.isBlank()) return "Event name is required"
        if (formData.date.isBlank()) return "Event date is required"
        if (formData.time.isBlank()) return "Event time is required"
        if (formData.location.isBlank()) return "Event location is required"
        if (formData.category.isBlank()) return "Event category is required"
        
        val rockZoneSeats = formData.rockZoneSeats.toIntOrNull() ?: 0
        val normalZoneSeats = formData.normalZoneSeats.toIntOrNull() ?: 0
        
        if (rockZoneSeats < 0 || normalZoneSeats < 0) {
            return "Seat numbers cannot be negative"
        }
        
        if (rockZoneSeats == 0 && normalZoneSeats == 0) {
            return "Total seats must be greater than 0"
        }

        val rockPrice = formData.rockZonePrice
        val normalPrice = formData.normalZonePrice

        if (rockPrice < 0) {
            return "Rock zone price cannot be negative"
        }

        if (normalPrice < 0) {
            return "Normal zone price cannot be negative"
        }
        
        return null
    }

    /**
     * Convert EventFormData to Event domain model
     * For create mode: eventId is empty, createdAt/updatedAt are generated
     * For edit mode: eventId, organizerId, organizerName, createdAt are preserved
     */
    private fun convertFormDataToEvent(
        formData: EventFormData,
        eventId: String = "",
        organizerId: String,
        organizerName: String,
        createdAt: Long = System.currentTimeMillis()
    ): Event {
        val rockZoneSeats = formData.rockZoneSeats.toIntOrNull() ?: 0
        val normalZoneSeats = formData.normalZoneSeats.toIntOrNull() ?: 0
        val totalSeats = rockZoneSeats + normalZoneSeats
        
        // Parse category
        val category = EventCategory.values().find { 
            it.displayName.equals(formData.category, ignoreCase = true) 
        } ?: EventCategory.FESTIVAL

        val isCreateMode = _uiState.value.event == null

        val availableSeats = if (isCreateMode) {
            totalSeats
        } else {
            _uiState.value.event?.availableSeats ?: totalSeats
        }

        val timeParts = formData.time.split("-").map { it.trim() }
        val startTime = timeParts.getOrNull(0)?.trim().orEmpty()
        val endTime = timeParts.getOrNull(1) ?: startTime

        return Event(
            eventId = eventId,
            name = formData.name,
            organizerId = organizerId,
            organizerName = organizerName,
            description = formData.description,
            category = category,
            status = _uiState.value.event?.status 
                ?: com.example.sera_application.domain.model.enums.EventStatus.PENDING,
            date = formData.date,
            startTime = startTime,
            endTime = endTime,
            duration = formData.duration,
            location = formData.location,
            rockZoneSeats = rockZoneSeats,
            normalZoneSeats = normalZoneSeats,
            totalSeats = totalSeats,
            availableSeats = availableSeats,
            rockZonePrice = formData.rockZonePrice,
            normalZonePrice = formData.normalZonePrice,
            imagePath = _uiState.value.imagePath
                ?: formData.imagePath
                ?: _uiState.value.event?.imagePath,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Reset success state
     */
    fun resetSuccessState() {
        _uiState.update { 
            it.copy(
                isSuccess = false, 
                createdEventId = null,
                imagePath = null
            ) 
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}




