package com.example.calenderintegration.repository

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import com.example.calenderintegration.api.googleapi.CalendarApiService
import com.example.calenderintegration.model.Event
import kotlinx.coroutines.flow.MutableStateFlow


import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val calendarService: CalendarApiService,
    private val accountsRepository: AccountsRepository
){

    // Cached events (shared across calls)
    private val _cachedEvents = MutableStateFlow<List<Event>>(emptyList())
    val cachedEvents = _cachedEvents.asStateFlow()

    /** Loads all events from Google Calendar. */
    suspend fun loadAllEvents(context: Context) {
        val accounts = accountsRepository.getGoogleAccounts(context) ?: emptyList()
        if (accounts.isEmpty()) {
            _cachedEvents.value = emptyList()
            return
        }

        val aggregated = mutableListOf<Event>()
        var remaining = accounts.size

        val deferred = kotlinx.coroutines.suspendCancellableCoroutine<List<Event>> { cont ->
            accounts.forEach { account ->
                calendarService.fetchCalendarData(context, account) { fetched ->
                    aggregated += fetched
                    remaining--
                    if (remaining == 0) {
                        cont.resume(aggregated, null)
                    }
                }
            }
        }

        _cachedEvents.value = deferred
        Log.d("EventRepository", "Fetched ${deferred.size} events.")
    }

    /** Returns event by ID from the cached list. */
    fun getEventById(id: String): Event? {
        return _cachedEvents.value.firstOrNull { it.id == id }
    }

    /** Updates an existing event and refreshes cache. */
    fun updateEvent(context: Context, updated: Event, onResult: (Boolean) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            Log.e("EventRepository", "Cannot update event: No saved accounts")
            onResult(false)
            return
        }

        calendarService.updateCalendarEvent(context, account, updated) { success ->
            if (success) {
                Log.d("EventRepository", "Event updated successfully. Refreshing cache.")
                runBlocking { loadAllEvents(context) }
            }
            onResult(success)
        }
    }

    /** Deletes an event and refreshes cache. */
    fun deleteEvent(context: Context, eventId: String, onResult: (Boolean) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            Log.e("EventRepository", "Cannot delete event: No saved accounts")
            onResult(false)
            return
        }

        calendarService.deleteCalendarEvent(context, account, eventId) { success ->
            if (success) {
                Log.d("EventRepository", "Event deleted successfully. Refreshing cache.")
                runBlocking { loadAllEvents(context) }
            }
            onResult(success)
        }
    }

    /** Creates a new event and refreshes cache. */
    fun createEvent(context: Context, event: Event, onResult: (Boolean) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            Log.e("EventRepository", "Cannot create event: No saved accounts")
            onResult(false)
            return
        }

        calendarService.createCalendarEvent(context, account, event) { success ->
            if (success) {
                Log.d("EventRepository", "Event created successfully. Refreshing cache.")
                runBlocking { loadAllEvents(context) }
            }
            onResult(success)
        }
    }
}
