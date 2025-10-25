package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
// androidx.compose.ui.test.filter // This import seems unused and can be removed
// REMOVE: import androidx.compose.ui.text.intl.Locale
import com.example.calenderintegration.model.Event
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter // Make sure this is java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.WeekFields
import java.util.Locale

// Inject a constructor to make hilt automatically add dependencies
@Singleton
class CalendarRepository @Inject constructor(
    val eventRepository: EventRepository
)
{
    /**
     * A helper function to parse date strings from the Google Calendar API.
     * It handles both full-day events ("2025-10-26") and timed events ("2018-07-18T07:00:00+02:00") [Google uses this one].
     * Antoine you can call this function wherever you need it to go from string date to LocalDate
     */

    /**
     * Data structure that holds both parsed date and formatted time information
     * extracted from a Google Calendar event’s start and end strings.
     * - `date`: The pure LocalDate (YYYY-MM-DD)
     * - `startTime`: Formatted start time (HH:mm) or "All day"
     * - `endTime`: Formatted end time (HH:mm) or empty if not defined
     */
    data class ParsedEventDate(
        val date: LocalDate?,      // The pure date (YYYY-MM-DD)
        val startTime: String,     // e.g. "07:30" or "All day"
        val endTime: String        // e.g. "09:00" or ""
    )

    /**
     * Parses event start and end date strings.
     * Extracts both LocalDate and formatted time for display (HH:mm),
     * handling both timed and all-day events.
     *
     * Logic:
     * - Determines if the string contains a 'T' to identify timed events.
     * - Converts ISO date-time strings into LocalDate and HH:mm format.
     * - Returns "All day" for all-day events (no time component).
     * - Returns empty strings for missing or invalid values.
     */
    fun parseEventStartDate(startString: String?, endString: String?): ParsedEventDate {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Helper function to safely parse a date string into LocalDate
        fun parseSingleDate(str: String?): LocalDate? {
            if (str.isNullOrBlank() || str == "No time specified") return null
            return try {
                if (str.contains('T')) {
                    OffsetDateTime.parse(str, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
                } else {
                    LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE)
                }
            } catch (e: DateTimeParseException) {
                Log.e("CalendarRepository", "Failed to parse date string: $str", e)
                null
            }
        }

        val date = parseSingleDate(startString)

        val startTime = try {
            if (!startString.isNullOrBlank() && startString.contains('T')) {
                OffsetDateTime.parse(startString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toLocalTime().format(timeFormatter)
            } else "All day"
        } catch (_: Exception) { "All day" }

        val endTime = try {
            if (!endString.isNullOrBlank() && endString.contains('T')) {
                OffsetDateTime.parse(endString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toLocalTime().format(timeFormatter)
            } else ""
        } catch (_: Exception) { "" }

        return ParsedEventDate(date, startTime, endTime)
    }




    /**
     * Filters events to return only those on a specific day.
     * Correctly includes timed and all-day events (Google all-day events use "date" fields).
     */
    fun getEventsByDay(date: LocalDate): List<Event> {
        val allEvents = eventRepository.events.value

        return allEvents.mapNotNull { event ->
            val parsed = parseEventStartDate(event.start, event.end)

            // Parse explicit start and end dates (may be same)
            val startDate = parsed.date
            val endDate = try {
                val parsedEnd = parseEventStartDate(event.end, event.end).date
                // Google all-day events have exclusive end; shift back by one day
                if (parsedEnd != null && !event.end.contains('T')) parsedEnd.minusDays(1)
                else parsedEnd
            } catch (_: Exception) { startDate }

            if (startDate == null) return@mapNotNull null

            val withinTimed = startDate.isEqual(date)
            val withinAllDay =
                endDate != null && // valid end
                        ( !event.start.contains('T') && // all-day type
                                ( !date.isBefore(startDate) && !date.isAfter(endDate) ) )

            if (withinTimed || withinAllDay) {
                event.copy(
                    start = parsed.startTime,
                    end = parsed.endTime
                )
            } else null
        }
    }

    /**
     * Filters events to return only those within a specific week.
     * @param date A LocalDate that falls within the target week.
     * @return A list of all events from all logged-in users for that week.
     *
     * Helper description:
     * - Uses `parseEventStartDate()` to extract LocalDate and time info.
     * - Calculates the ISO week number of the target date and event date.
     * - Includes both timed and all-day events that fall in the same week and year.
     * - Returns events with formatted start/end times for display.
     */
    fun getEventsByWeek(date: LocalDate): List<Event> {
        val allEvents = eventRepository.events.value
        val weekFields = WeekFields.of(Locale.getDefault())
        val targetWeek = date.get(weekFields.weekOfYear())
        val targetYear = date.year

        return allEvents.mapNotNull { event ->
            val parsed = parseEventStartDate(event.start, event.end)
            val eventDate = parsed.date

            // Include event if it occurs in the same ISO week and year
            if (eventDate != null &&
                eventDate.year == targetYear &&
                eventDate.get(weekFields.weekOfYear()) == targetWeek) {
                event.copy(
                    start = parsed.startTime,
                    end = parsed.endTime
                )
            } else null
        }
    }

    /**
     * Filters events to return only those within a specific month.
     * @param date A LocalDate that falls within the target month.
     * @return A list of all events from all logged-in users for that month.
     *
     * Helper description:
     * - Uses `parseEventStartDate()` to normalize all date/time formats.
     * - Includes both timed and all-day events occurring in the same month and year.
     * - Returns event copies with formatted start/end time strings for UI use.
     */
    fun getEventsByMonth(date: LocalDate): List<Event> {
        val allEvents = eventRepository.events.value

        return allEvents.mapNotNull { event ->
            val parsed = parseEventStartDate(event.start, event.end)
            val eventDate = parsed.date

            // Include event if it falls in the same month and year
            if (eventDate != null &&
                eventDate.year == date.year &&
                eventDate.month == date.month) {
                event.copy(
                    start = parsed.startTime,
                    end = parsed.endTime
                )
            } else null
        }
    }



}