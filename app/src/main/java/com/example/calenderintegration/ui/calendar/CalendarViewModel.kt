package com.example.calenderintegration.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repo: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    fun switchMode(mode: CalendarMode) {
        _uiState.update { it.copy(currentMode = mode)}
    }

    fun getAllEvents() {
        // get all Events from the repo
        viewModelScope.launch {
            // set uiState to loading
            _uiState.update { it.copy(isLoading = true)}
            try {
                val events: List<Event> = repo.loadAllEvents()
                // add events list to uiState
                _uiState.update { it.copy(events = events) }

            } catch (e: Exception) {
                // set uiState to error
                _uiState.update { it.copy(isLoading = false, error = e.message)}
            }
        }
    }
}

