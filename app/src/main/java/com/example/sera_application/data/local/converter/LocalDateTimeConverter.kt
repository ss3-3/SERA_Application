package com.example.sera_application.data.local.converter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
    }
}

