package com.example.calenderintegration.ui.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calenderintegration.model.Event

@Composable
fun CalendarScreen (
    calendarViewModel: CalendarViewModel = viewModel(),
    forceMode: CalendarMode? = null,
    onEventNavigate: (event: Event) -> Unit,
    modifier: Modifier = Modifier
){
    val uiState by calendarViewModel.uiState.collectAsState()

    // if forceMode arg is not added, mode will return to previous view
    val mode = forceMode ?: uiState.currentMode

    when (mode) {
        CalendarMode.DAILY -> { DailyView(calendarViewModel,
            uiState,
            onEventClick = onEventNavigate,
            modifier
        )}
        CalendarMode.WEEKLY -> { WeeklyView(calendarViewModel ,
            uiState,
            onEventClick = { event -> onEventNavigate(event)},
            modifier
        ) }
        CalendarMode.MONTHLY -> { MonthlyView(calendarViewModel,
            uiState,
            onEventClick = onEventNavigate,
            modifier
        ) }
    }
}