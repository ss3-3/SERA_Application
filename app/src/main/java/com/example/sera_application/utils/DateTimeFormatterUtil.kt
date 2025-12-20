package com.example.sera_application.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeFormatterUtil {

    private val dateTimeFormatter =
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    fun parse(date: String, time: String): Long {
        return dateTimeFormatter.parse("$date $time")!!.time
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    private fun parseTimeToTimestamp(time: String): Long {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.parse(time)!!.time
    }

    fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun formatTimeRange(start: Long, end: Long): String {
        return "${formatTime(start)} - ${formatTime(end)}"
    }
}
