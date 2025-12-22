package com.example.sera_application.utils

import java.util.Calendar
import java.util.Locale

object DateUtils {
    
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    /**
     * Get current month string in format "Month YYYY" (e.g., "January 2025")
     */
    fun getCurrentMonthString(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "${monthNames[month]} $year"
    }
    
    /**
     * Generate list of months for a specific year in format "Month YYYY"
     */
    fun getMonthList(year: Int? = null): List<String> {
        val calendar = Calendar.getInstance()
        val targetYear = year ?: calendar.get(Calendar.YEAR)
        
        return monthNames.map { "$it $targetYear" }
    }
    
    /**
     * Get list of available years (current year and previous years)
     * @param yearsBack Number of years to go back from current year (default: 5)
     */
    fun getYearList(yearsBack: Int = 5): List<Int> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        return (currentYear downTo currentYear - yearsBack).toList()
    }
    
    /**
     * Get current year
     */
    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }
    
    /**
     * Get previous month from given month string
     * @param currentMonth Month string in format "Month YYYY"
     * @return Previous month string or null if currentMonth is null or January
     */
    fun getPreviousMonth(currentMonth: String?): String? {
        if (currentMonth == null) return null
        
        val parts = currentMonth.split(" ")
        if (parts.size != 2) return null
        
        val monthName = parts[0]
        val year = parts[1].toIntOrNull() ?: return null
        
        val currentIndex = monthNames.indexOf(monthName)
        if (currentIndex == -1) return null
        
        return if (currentIndex > 0) {
            // Previous month in same year
            "${monthNames[currentIndex - 1]} $year"
        } else {
            // Previous year, December
            "${monthNames[11]} ${year - 1}"
        }
    }
    
    /**
     * Parse month string to start and end timestamps
     * @param month Month string in format "Month YYYY"
     * @return Pair of (startTimestamp, endTimestamp) or null if parsing fails
     */
    fun parseMonthToTimestamp(month: String?): Pair<Long, Long>? {
        if (month == null) return null
        
        val parts = month.split(" ")
        if (parts.size != 2) return null
        
        val monthName = parts[0]
        val year = parts[1].toIntOrNull() ?: return null
        
        val monthIndex = monthNames.indexOf(monthName)
        if (monthIndex == -1) return null
        
        val calendar = Calendar.getInstance()
        
        // Start of month (first day, 00:00:00)
        calendar.set(year, monthIndex, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTimestamp = calendar.timeInMillis
        
        // End of month (last day, 23:59:59.999)
        calendar.set(year, monthIndex, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTimestamp = calendar.timeInMillis
        
        return Pair(startTimestamp, endTimestamp)
    }
}

