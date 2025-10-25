package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
import com.example.calenderintegration.api.googleapi.CalendarApiService
import com.example.calenderintegration.model.Event


import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Singleton

// Inject a constructor to make hilt automatically add dependencies
@Singleton
class EventRepository @Inject constructor(
    private val calendarService : CalendarApiService,
    val authRepository: AuthRepository
)
{
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    val uiScope = CoroutineScope(Dispatchers.Main)

    /**
     * Fetches upcoming calendar events for the signed-in user.
     */
    fun fetchEvents(context: Context) {
        val accounts = authRepository.accounts.value
        if (accounts.isEmpty()) {
            Log.e("GoogleAPI", "Cannot fetch events: No signed-in accounts")
            _events.value = emptyList()
            return
        }

        val aggregated = mutableListOf<Event>()

        accounts.forEach { account ->
            calendarService.fetchCalendarData(context, account) { fetchedEvents ->
                uiScope.launch {
                    aggregated += fetchedEvents
                    _events.value = aggregated
                    Log.d("GoogleAPI", "Fetched ${fetchedEvents.size} from ${account.email}, total=${aggregated.size}")
                }
            }
        }
    }





    /**
     *
     * Gets an event by id. Returns if found, returns null if not
     */
    fun getEventById(context : Context, id: String): Event?
    {
        // Make sure _events is populated
        fetchEvents(context)

        return _events.value.find { it.id == id }
    }

    /**
     *
     * Updates an event. Returns the updated event if found, returns null if not
     */
    fun updateEvent(id: String, changedEvent: Event): Event?
    {

        val eventToChange = _events.value.find { it.id == id }

        if (eventToChange != null)
        {
            eventToChange.summary = changedEvent.summary
            eventToChange.description = changedEvent.description
            eventToChange.location = changedEvent.location
            eventToChange.start = changedEvent.start
            eventToChange.end = changedEvent.end
            return eventToChange
        }

        return null
    }

    /**
     * Creates a calendar event for the signed-in user.
     */
    fun createEvent(context : Context, event: Event, onResult: (Boolean) -> Unit) {
        val account = authRepository.currentAccount.value
        if (account == null) {
            Log.e("GoogleAPI", "Cannot create event: No signed-in account")
            onResult(false)
            return
        }

        calendarService.createCalendarEvent(context,account, event, onResult)
    }


    /**
     * Deletes a calendar event for the signed-in user.
     */
    fun deleteEvent(context: Context, eventId: String, onResult: (Boolean) -> Unit) {
        val account = authRepository.currentAccount.value
        if (account == null) {
            Log.e("GoogleAPI", "Cannot delete event: No signed-in account")
            onResult(false)
            return
        }

        calendarService.deleteCalendarEvent(context,account, eventId, onResult)
    }

}