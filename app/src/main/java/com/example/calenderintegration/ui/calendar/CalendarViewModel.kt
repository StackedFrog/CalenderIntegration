package com.example.calenderintegration.ui.calendar

import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    /**
     * Fetches all events from all logged-in accounts and updates
     * the daily, weekly, and monthly event lists once data is received.
     */
    fun loadAllEvents(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Trigger the fetch for all signed-in accounts
            repo.eventRepository.fetchEvents(context)

            // Collect and react to updates in the event list
            repo.eventRepository.events.collectLatest { allEvents ->
                val today = LocalDate.now()

                val daily = repo.getEventsByDay(today)
                val weekly = repo.getEventsByWeek(today)
                val monthly = repo.getEventsByMonth(today)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailyEvents = daily,
                        weeklyEvents = weekly,
                        monthlyEvents = monthly
                    )
                }

                Log.d(
                    "CalendarVM",
                    "✅ Loaded ${daily.size} daily, ${weekly.size} weekly, ${monthly.size} monthly events"
                )
            }
        }
    }

    /**
     * Fetches and filters events for a specific day.
     */
    fun getEventsForDay(context: Context, date: LocalDate) {
        viewModelScope.launch {
            repo.eventRepository.fetchEvents(context)
            val events = repo.getEventsByDay(date)
            _uiState.update { it.copy(dailyEvents = events) }
            Log.d("CalendarVM", "Fetched ${events.size} events for $date")
        }
    }

    /**
     * Fetches and filters all events for the current week.
     */
    fun getAllEventsThisWeek() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val events = repo.getEventsByWeek(LocalDate.now())
                _uiState.update { it.copy(weeklyEvents = events, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Refreshes all events for all signed-in accounts.
     * Used when a new account is added or events change.
     */
    fun refresh(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.eventRepository.fetchEvents(context)
        }
    }
}


