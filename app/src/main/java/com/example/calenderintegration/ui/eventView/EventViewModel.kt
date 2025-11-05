package com.example.calenderintegration.ui.eventView

import androidx.lifecycle.ViewModel
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

import android.content.Context

import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.api.googleapi.CalendarApiService
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.model.GoogleAccount
import kotlinx.coroutines.launch


import com.example.calenderintegration.repository.AccountsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EventViewModel : ViewModel() {

    private val repo = EventRepository(
        calendarService = CalendarApiService,
        accountsRepository = AccountsRepository(googleStore = GoogleAccountRepository)
    )

    private val _eventState = MutableStateFlow(EventState())
    val eventState: StateFlow<EventState> = _eventState

    fun loadEvent(context: Context, eventId: String) {
        viewModelScope.launch {
            if (repo.cachedEvents.value.isEmpty()) {
                repo.loadAllEvents(context)
            }

            val event = repo.getEventById(eventId)
            _eventState.value = if (event != null) {
                EventState(event = event)
            } else {
                EventState(error = "Event not found")
            }
        }
    }

    fun formatDateTimeForDisplay(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            if (raw.contains('T')) {
                val dateTime = OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            } else {
                LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }
        } catch (e: Exception) {
            raw
        }
    }

    fun formatDateTimeForSave(date: LocalDate, time: LocalTime?): String {
        return if (time == null) {
            date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            val dateTime = ZonedDateTime.of(date, time, ZoneId.systemDefault())
            dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
    }


    fun deleteEvent(context: Context, eventId: String) {
        repo.deleteEvent(context, eventId) { success ->
            _eventState.value = EventState(
                error = if (success) "Event deleted" else "Delete failed"
            )
        }
    }

    fun createEvent(context: Context, event: Event, onResult: (Boolean) -> Unit) {
        repo.createEvent(context, event) { success ->
            _eventState.value = if (success) EventState(event = event)
            else EventState(error = "Failed to create")
            onResult(success)
        }
    }

    fun updateEvent(context: Context, event: Event, onResult: (Boolean) -> Unit) {
        repo.updateEvent(context, event) { success ->
            _eventState.value = if (success) EventState(event = event)
            else EventState(error = "Failed to update")
            onResult(success)
        }
    }

    fun resetEventState() {
        _eventState.value = EventState(event = null, error = null)
    }


    fun createEmptyEvent(): Event {
        return Event(
            id = "",
            summary = "",
            description = "",
            location = "",
            start = "",
            end = "",
            calendarEmail = "",
            etag = ""
        )
    }
}

data class EventState(
    val event: Event? = null,
    val error: String? = null
)
