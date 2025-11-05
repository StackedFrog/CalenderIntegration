package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
import com.example.calenderintegration.api.googleapi.CalendarApiService
// androidx.compose.ui.test.filter // This import seems unused and can be removed
// REMOVE: import androidx.compose.ui.text.intl.Locale
import com.example.calenderintegration.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter // Make sure this is java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.WeekFields
import java.util.Locale


@Singleton
class CalendarRepository @Inject constructor(
    private val calendarService: CalendarApiService,
    private val accountsRepository: AccountsRepository
) {
    private val _cachedEvents = MutableStateFlow<List<Event>>(emptyList())
    val cachedEvents: StateFlow<List<Event>> = _cachedEvents

    /**
     * Fetches all calendar events from all saved Google accounts.
     * Returns a combined list instead of managing state internally.
     */
    suspend fun fetchEvents(context: Context): List<Event> = suspendCancellableCoroutine { cont ->
        val accounts = accountsRepository.getGoogleAccounts(context) ?: emptyList()

        if (accounts.isEmpty()) {
            Log.w("CalendarRepository", "No saved accounts; returning empty list")
            _cachedEvents.value = emptyList()
            cont.resume(emptyList(), null)
            return@suspendCancellableCoroutine
        }

        val aggregated = mutableListOf<Event>()
        var remaining = accounts.size

        accounts.forEach { account ->
            calendarService.fetchCalendarData(context, account) { fetchedEvents ->
                aggregated += fetchedEvents
                remaining--

                if (remaining == 0) {
                    Log.d("CalendarRepository", "Fetched ${aggregated.size} total events")
                    _cachedEvents.value = aggregated // <-- cache update
                    cont.resume(aggregated, null)
                }
            }
        }
    }


    // region ---------- Parsing + Filtering ----------
    data class ParsedEventDate(
        val date: LocalDate?,
        val startTime: String,
        val endTime: String
    )

    fun parseEventStartDate(startString: String?, endString: String?): ParsedEventDate {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

    fun getEventsByDay(allEvents: List<Event>, date: LocalDate): List<Event> {
        return allEvents.mapNotNull { event ->
            val parsed = parseEventStartDate(event.start, event.end)
            val startDate = parsed.date ?: return@mapNotNull null
            val endDate = try {
                val parsedEnd = parseEventStartDate(event.end, event.end).date
                if (parsedEnd != null && !event.end.contains('T')) parsedEnd.minusDays(1)
                else parsedEnd
            } catch (_: Exception) { startDate }

            val withinTimed = startDate.isEqual(date)
            val withinAllDay =
                endDate != null && !event.start.contains('T') &&
                        (!date.isBefore(startDate) && !date.isAfter(endDate))

            if (withinTimed || withinAllDay)
                event.copy(start = parsed.startTime, end = parsed.endTime)
            else null
        }
    }

    fun getEventsByWeek(allEvents: List<Event>, date: LocalDate): List<Event> {
        val weekFields = WeekFields.of(Locale.getDefault())
        val targetWeek = date.get(weekFields.weekOfYear())
        val targetYear = date.year

        val startOfWeek = date.with(weekFields.dayOfWeek(), 1)
        val endOfWeek = startOfWeek.plusDays(6)

        return allEvents.mapNotNull { event ->
            val parsedStart = parseEventStartDate(event.start, event.end)
            val parsedEnd = parseEventStartDate(event.end, event.end)

            val startDate = parsedStart.date ?: return@mapNotNull null
            val endDate = parsedEnd.date ?: startDate

            // Include if event overlaps any part of this week
            val overlaps =
                !(endDate.isBefore(startOfWeek) || startDate.isAfter(endOfWeek))

            if (overlaps) {
                event.copy(
                    start = parsedStart.startTime,
                    end = parsedStart.endTime
                )
            } else null
        }
    }

    fun getEventsByMonth(allEvents: List<Event>, date: LocalDate): List<Event> {
        val startOfMonth = date.withDayOfMonth(1)
        val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

        return allEvents.mapNotNull { event ->
            val parsedStart = parseEventStartDate(event.start, event.end)
            val parsedEnd = parseEventStartDate(event.end, event.end)

            val startDate = parsedStart.date ?: return@mapNotNull null
            val endDate = parsedEnd.date ?: startDate

            // Include if event overlaps any part of this month
            val overlaps =
                !(endDate.isBefore(startOfMonth) || startDate.isAfter(endOfMonth))

            if (overlaps) {
                event.copy(
                    start = parsedStart.startTime,
                    end = parsedStart.endTime
                )
            } else null
        }
    }



    fun getEventById(id: String): Event? {
        return _cachedEvents.value.firstOrNull { it.id == id }
    }


    // endregion
}