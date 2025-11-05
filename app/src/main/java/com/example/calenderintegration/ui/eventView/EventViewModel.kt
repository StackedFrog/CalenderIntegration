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
class EventViewModel : ViewModel() {

    private val repo = EventRepository(
        calendarService = CalendarApiService,
        accountsRepository = AccountsRepository(GoogleAccountRepository)
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
            calendarEmail = ""
        )
    }
}

data class EventState(
    val event: Event? = null,
    val error: String? = null
)
