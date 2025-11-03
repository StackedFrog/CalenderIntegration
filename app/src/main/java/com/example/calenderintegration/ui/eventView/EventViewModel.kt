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
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import kotlinx.coroutines.launch


import com.example.calenderintegration.repository.AccountsRepository
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    // Build your dependencies internally — no constructor arguments
    private val repo = EventRepository(
        calendarService = CalendarApiService,
        accountsRepository = AccountsRepository(GoogleAccountRepository, ZohoAccountRepository())
    )
    private val _cachedEvents = MutableStateFlow<List<Event>>(emptyList())
    val cachedEvents: StateFlow<List<Event>> = _cachedEvents
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

    fun updateEvent(context: Context, event: Event) {
        repo.updateEvent(context, event) { success ->
            _eventState.value = if (success) EventState(event = event)
            else EventState(error = "Failed to update")
        }
    }

    fun deleteEvent(context: Context, eventId: String) {
        repo.deleteEvent(context, eventId) { success ->
            _eventState.value = EventState(
                error = if (success) "Event deleted" else "Delete failed"
            )
        }
    }


data class EventState(
    val event: Event? = null,
    val error: String? = null
)
}
