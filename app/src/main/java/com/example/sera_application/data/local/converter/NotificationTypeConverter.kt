package com.example.sera_application.data.local.converter

import androidx.room.TypeConverter
import com.example.sera_application.domain.model.enums.NotificationType

class NotificationTypeConverter {
    @TypeConverter
    fun fromNotificationType(type: NotificationType): String {
        return type.name
    }

    @TypeConverter
    fun toNotificationType(value: String): NotificationType {
        return try {
            NotificationType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            NotificationType.SYSTEM // Default fallback
        }
    }
}

