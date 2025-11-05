package com.example.calenderintegration.ui.eventView

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.EventRepository
import com.example.calenderintegration.repository.AccountsRepository
import com.example.calenderintegration.api.googleapi.CalendarApiService
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EventViewModel : ViewModel() {

    private val repo = EventRepository(
        calendarService = CalendarApiService,
        accountsRepository = AccountsRepository(GoogleAccountRepository)
    )

    private val _eventState = MutableStateFlow(EventState())
    val eventState: StateFlow<EventState> = _eventState

    fun loadEvent(context: Context, eventId: String) {
        if (eventId == "new") {
            _eventState.value = EventState(
                event = Event(
                    id = "",
                    summary = "",
                    description = "",
                    location = "",
                    start = "",
                    end = "",
                    calendarEmail = "",
                    calendarId = null,
                    isEditable = true
                ),
                isEditing = true
            )
            return
        }

        _eventState.value = _eventState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                if (repo.cachedEvents.value.isEmpty()) {
                    repo.loadAllEvents(context)
                }
                val event = repo.getEventById(eventId)
                _eventState.value = if (event != null) {
                    EventState(event = event, isLoading = false)
                } else {
                    EventState(error = "Event not found", isLoading = false)
                }
            } catch (t: Throwable) {
                _eventState.value = EventState(error = t.message ?: "Load failed", isLoading = false)
            }
        }
    }

    fun startEditing() {
        _eventState.value = _eventState.value.copy(isEditing = true, error = null)
    }

    fun cancelEditing() {
        _eventState.value = _eventState.value.copy(isEditing = false, error = null)
    }

    fun confirmEdit(context: Context, event: Event) {
        if (_eventState.value.isSaving) return
        _eventState.value = _eventState.value.copy(isSaving = true, error = null)

        // Normalize user input to Google-friendly values:
        // - "" => leave blank (server will complain if both empty)
        // - "yyyy-MM-dd" => all-day (use 'date')
        // - "yyyy-MM-dd HH:mm" => RFC3339 UTC "yyyy-MM-dd'T'HH:mm:00Z"
        val normStart = normalizeForApi(event.start)
        val normEnd = normalizeForApi(event.end)

        val normalized = event.copy(start = normStart, end = normEnd)
        val isNew = normalized.id.isBlank()

        if (isNew) {
            repo.createEvent(context, normalized) { success, message ->
                if (success) {
                    _eventState.value = _eventState.value.copy(
                        event = normalized, isEditing = false, isSaving = false, error = null
                    )
                } else {
                    _eventState.value = _eventState.value.copy(
                        isSaving = false, error = message ?: "Failed to create"
                    )
                }
            }
        } else {
            repo.updateEvent(context, normalized) { success, message ->
                if (success) {
                    _eventState.value = _eventState.value.copy(
                        event = normalized, isEditing = false, isSaving = false, error = null
                    )
                } else {
                    _eventState.value = _eventState.value.copy(
                        isSaving = false, error = message ?: "Failed to update"
                    )
                }
            }
        }
    }

    private val dateOnlyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeInputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val rfc3339Out = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private fun normalizeForApi(input: String): String {
        val s = input.trim()
        if (s.isEmpty()) return s

        // All-day: exactly yyyy-MM-dd
        if (s.length == 10 && s[4] == '-' && s[7] == '-') {
            // "2025-11-06"
            return s
        }

        // If user already pasted an RFC3339 or contains 'T', pass through
        if (s.contains('T')) return s

        // Try parsing "yyyy-MM-dd HH:mm" -> RFC3339 UTC
        return try {
            val ldt = LocalDateTime.parse(s, dateTimeInputFmt)
            ldt.atOffset(ZoneOffset.UTC).format(rfc3339Out)
        } catch (_: Throwable) {
            // Try "yyyy-MM-dd" (just in case)
            try {
                LocalDate.parse(s, dateOnlyFmt).toString()
            } catch (_: Throwable) {
                s // fallback: send as-is (server may reject; error will be shown)
            }
        }
    }
}

data class EventState(
    val event: Event? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false
)
