package com.example.calenderintegration.repository

import android.util.Log
// androidx.compose.ui.test.filter // This import seems unused and can be removed
// REMOVE: import androidx.compose.ui.text.intl.Locale
import com.example.calenderintegration.model.Event
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter // Make sure this is java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.WeekFields
import java.util.Locale

// Inject a constructor to make hilt automatically add dependencies
class CalendarRepository @Inject constructor(
    private val eventRepository: EventRepository
)
{

    /**
     * A helper function to parse date strings from the Google Calendar API.
     * It handles both full-day events ("2025-10-26") and timed events ("2018-07-18T07:00:00+02:00") [Google uses this one].
     * Antoine you can call this function wherever you need it to go from string date to LocalDate
     */
    fun parseEventStartDate(dateString: String): LocalDate? {
        return try {
            // Case 1: Timed event (e.g., "2018-07-18T07:00:00+02:00")
            // This is the most common format for non-all-day events.
            if (dateString.contains('T')) {
                OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
            } else {
                // Case 2: All-day event (e.g., "2025-10-26")
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            }
        } catch (e: DateTimeParseException) {
            Log.e("CalendarRepository", "Failed to parse date string: $dateString", e)
            null // Return null if the date string is malformed
        }
    }

    /**
     * Filters events to return only those within a specific month.
     * @param date A LocalDate that falls within the target month.
     * @return A list of all events from all logged-in users for that month.
     */
    fun getEventsByMonth(date: LocalDate): List<Event>
    {
        // Get the single source of truth for events
        val allEvents = eventRepository.events.value

        return allEvents.filter { event ->
            // Parse the event's start date string
            val eventStartDate = parseEventStartDate(event.start)
            // Check if the event's date matches the target year and month
            eventStartDate != null &&
                    eventStartDate.year == date.year &&
                    eventStartDate.month == date.month
        }
    }

    /**
     * Filters events to return only those within a specific week.
     * @param date A LocalDate that falls within the target week.
     * @return A list of all events from all logged-in users for that week.
     */
    fun getEventsByWeek(date: LocalDate): List<Event> {
        val allEvents = eventRepository.events.value

        // Use WeekFields to define what constitutes a week (e.g., starts on Monday)
        val weekFields = WeekFields.of(Locale.getDefault())
        val targetWeek = date.get(weekFields.weekOfYear())
        val targetYear = date.year

        return allEvents.filter { event ->
            val eventStartDate = parseEventStartDate(event.start)
            // Check if the event's date falls within the same year and week number
            eventStartDate != null &&
                    eventStartDate.year == targetYear &&
                    eventStartDate.get(weekFields.weekOfYear()) == targetWeek
        }
    }
    /**
     * Filters events to return only those on a specific day.
     * @param date The specific day to filter by.
     * @return A list of all events from all logged-in users for that day.
     */
    fun getEventsByDay(date: LocalDate): List<Event> {
        val allEvents = eventRepository.events.value

        return allEvents.filter { event ->
            val eventStartDate = parseEventStartDate(event.start)
            // Check if the event's date is exactly equal to the target date
            eventStartDate != null && eventStartDate.isEqual(date)
        }
    }
}

