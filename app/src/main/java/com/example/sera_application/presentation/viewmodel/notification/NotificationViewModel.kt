package com.example.sera_application.presentation.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sera_application.domain.model.Notification
import com.example.sera_application.domain.usecase.notification.GetUserNotificationsUseCase
import com.example.sera_application.domain.usecase.notification.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getUserNotificationsUseCase: GetUserNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val unreadCount: StateFlow<Int> = _notifications.map { notifications ->
        notifications.count { !it.isRead }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = getUserNotificationsUseCase(userId)
                result.fold(
                    onSuccess = { notifications ->
                        _notifications.value = notifications
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Failed to load notifications"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load notifications"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = markNotificationAsReadUseCase(notificationId)
                result.fold(
                    onSuccess = {
                        _notifications.value = _notifications.value.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Failed to mark notification as read"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to mark notification as read"
            }
        }
    }
}
