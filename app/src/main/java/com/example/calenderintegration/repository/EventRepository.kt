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
    private val calendarService: CalendarApiService,
    private val accountsRepository: AccountsRepository
) {
    /**
     * Retrieves a specific event by ID.
     * (Optional: could query backend if needed)
     */
    fun getEventById(context: Context, id: String): Event? {
        // Not ideal — normally fetched from cached data managed by CalendarRepository
        return null
    }

    /**
     * Updates an existing event (local model-level change).
     */
    fun updateEvent(id: String, changedEvent: Event): Event {
        // You could later make this push changes to Google Calendar
        return changedEvent
    }

    /**
     * Creates a new calendar event for the first available account.
     */
    fun createEvent(context: Context, event: Event, onResult: (Boolean) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            Log.e("EventRepository", "Cannot create event: No saved accounts")
            onResult(false)
            return
        }

        calendarService.createCalendarEvent(context, account, event, onResult)
    }

    /**
     * Deletes a calendar event for the first available account.
     */
    fun deleteEvent(context: Context, eventId: String, onResult: (Boolean) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            Log.e("EventRepository", "Cannot delete event: No saved accounts")
            onResult(false)
            return
        }

        calendarService.deleteCalendarEvent(context, account, eventId, onResult)
    }
}
