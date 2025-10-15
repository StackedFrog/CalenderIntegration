package com.example.calenderintegration.ui.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalendarScreen (
    calendarViewModel: CalendarViewModel = viewModel(),
    forceMode: CalendarMode? = null,
    modifier: Modifier = Modifier
){
    // TODO: IMPROVE STRUCUTRE
    val uiState by calendarViewModel.uiState.collectAsState()

    var mode = uiState.currentMode

    if (forceMode !== null) {
        mode = forceMode
    }

    when (mode) {
        CalendarMode.DAILY -> { DailyView(uiState, modifier)}
        CalendarMode.WEEKLY -> { WeeklyView(uiState, modifier) }
        CalendarMode.MONTHLY -> { MonthlyView(uiState, modifier) }
    }
}