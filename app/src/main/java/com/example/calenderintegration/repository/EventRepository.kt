package com.example.calenderintegration.repository

import android.content.Context
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
) {
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
                    if (remaining == 0) cont.resume(aggregated, null)
                }
            }
        }

        _cachedEvents.value = deferred
        Log.d("EventRepository", "Fetched ${deferred.size} events.")
    }

    /** Returns event by ID from the cached list. */
    fun getEventById(id: String): Event? = _cachedEvents.value.firstOrNull { it.id == id }

    /**
     * Try to update. If Google rejects (e.g., read-only Birthday/Holiday),
     * automatically create a NEW event on primary with the edited values.
     *
     * onResult: success, message
     */
    fun updateEvent(context: Context, updated: Event, onResult: (Boolean, String?) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            onResult(false, "No saved accounts")
            return
        }

        calendarService.updateCalendarEvent(context, account, updated) { success ->
            if (success) {
                // refresh cache
                runBlocking { loadAllEvents(context) }
                onResult(true, null)
            } else {
                // Fallback: save as a NEW event on primary (copy values, clear id)
                val copy = updated.copy(id = "")
                calendarService.createCalendarEvent(context, account, copy) { created ->
                    if (created) {
                        runBlocking { loadAllEvents(context) }
                        onResult(true, "Original was read-only; saved as a new event.")
                    } else {
                        onResult(false, "Update failed and could not create a new event.")
                    }
                }
            }
        }
    }

    /** Creates a new event and refreshes cache. */
    fun createEvent(context: Context, event: Event, onResult: (Boolean, String?) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            onResult(false, "No saved accounts")
            return
        }

        calendarService.createCalendarEvent(context, account, event) { success ->
            if (success) {
                runBlocking { loadAllEvents(context) }
                onResult(true, null)
            } else {
                onResult(false, "Create failed")
            }
        }
    }

    /** Deletes an event and refreshes cache. */
    fun deleteEvent(context: Context, eventId: String, onResult: (Boolean, String?) -> Unit) {
        val account = accountsRepository.getGoogleAccounts(context)?.firstOrNull()
        if (account == null) {
            onResult(false, "No saved accounts")
            return
        }

        calendarService.deleteCalendarEvent(context, account, eventId) { success ->
            if (success) {
                runBlocking { loadAllEvents(context) }
                onResult(true, null)
            } else {
                onResult(false, "Delete failed")
            }
        }
    }
}
