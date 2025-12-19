package com.example.sera_application.data.local.converter

import androidx.room.TypeConverter
import com.example.sera_application.domain.model.enums.EventCategory

class EventCategoryConverter {

    @TypeConverter
    fun fromCategory(category: EventCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): EventCategory {
        return EventCategory.valueOf(value)
    }
}