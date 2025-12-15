package com.example.sera_application.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DateTimeUtils @Inject constructor() {

    fun formatTimestamp(timestamp: Long, pattern: String = "dd MMM yyyy, HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return formatTimestamp(timestamp, "dd MMM yyyy")
    }

    fun formatTime(timestamp: Long): String {
        return formatTimestamp(timestamp, "HH:mm")
    }

    fun getHoursUntil(futureTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Long {
        val diff = futureTimestamp - currentTimestamp
        return TimeUnit.MILLISECONDS.toHours(diff)
    }

    fun getDaysUntil(futureTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Long {
        val diff = futureTimestamp - currentTimestamp
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun isEventPassed(eventTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        return currentTimestamp > eventTimestamp
    }

    fun isWithinCancellationWindow(
        reservationTimestamp: Long,
        eventTimestamp: Long,
        currentTimestamp: Long = System.currentTimeMillis(),
        hoursBeforeEvent: Long = 24
    ): Boolean {
        val cancellationDeadline = eventTimestamp - TimeUnit.HOURS.toMillis(hoursBeforeEvent)
        return currentTimestamp <= cancellationDeadline
    }

    fun getRelativeTimeString(timestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): String {
        val diff = timestamp - currentTimestamp
        
        return when {
            diff < 0 -> "Past event"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "In $minutes minutes"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "In $hours hours"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "In $days days"
            }
            else -> formatDate(timestamp)
        }
    }
}
