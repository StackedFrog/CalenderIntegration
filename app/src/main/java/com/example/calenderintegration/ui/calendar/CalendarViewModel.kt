package com.example.calenderintegration.ui.calendar

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    private var isFetching = false

    fun loadAllEvents(context: Context) {
        if (isFetching) {
            Log.d("CalendarVM", "Skipping fetch: already running")
            return
        }
        isFetching = true
        Log.d("CalendarVM", "Starting loadAllEvents")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d("CalendarVM", "State set to loading")

            try {
                val allEvents = calendarRepository.fetchEvents(context)
                Log.d("CalendarVM", "fetchEvents returned ${allEvents.size} events")

                val today = LocalDate.now()

                _uiState.update {
                    it.copy(
                        allEvents = allEvents,
                        dailyEvents = calendarRepository.getEventsByDay(allEvents, today),
                        weeklyEvents = calendarRepository.getEventsByWeek(allEvents, today),
                        monthlyEvents = calendarRepository.getEventsByMonth(allEvents, today),
                        isLoading = false,
                        error = null
                    )
                }
                Log.d(
                    "CalendarVM",
                    "UI updated: daily=${_uiState.value.dailyEvents.size}, weekly=${_uiState.value.weeklyEvents.size}, monthly=${_uiState.value.monthlyEvents.size}, isLoading=${_uiState.value.isLoading}"
                )
            } catch (e: Exception) {
                Log.e("CalendarVM", "Failed to fetch events", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            } finally {
                isFetching = false
                Log.d("CalendarVM", "Fetch complete, isFetching=false")
            }
        }
    }

    fun startAutoRefresh(context: Context) {
        viewModelScope.launch {
            // Initial fetch once
            loadAllEvents(context)

            // Then refresh every 2 minutes
            while (isActive) {
                delay(2 * 60 * 1000L)
                loadAllEvents(context)
            }
        }
    }



    fun getEventsForDay(date: LocalDate): List<Event> {
        val all = _uiState.value.allEvents
        return calendarRepository.getEventsByDay(all, date)
    }

    fun getEventsForWeek(date: LocalDate): List<Event> {
        val all = _uiState.value.allEvents
        return calendarRepository.getEventsByWeek(all, date)
    }

    fun getEventsForMonth(date: LocalDate): List<Event> {
        val all = _uiState.value.allEvents
        return calendarRepository.getEventsByMonth(all, date)
    }
}

