package com.example.calenderintegration.ui.calendar

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calenderintegration.model.Event
import java.time.LocalDate

@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
    forceMode: CalendarMode? = null,
    onEventNavigate: (event: Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by calendarViewModel.uiState.collectAsState()

    // Load all events once if not already loaded
    LaunchedEffect(calendarViewModel) {
        if (!uiState.isLoading && uiState.allEvents.isEmpty()) {
            calendarViewModel.loadAllEvents(context)
        }
    }

    val mode = forceMode ?: uiState.currentMode

    when (mode) {
        CalendarMode.DAILY -> DailyView(
            context = context,
            calendarViewModel = calendarViewModel,
            uiState = uiState,
            selectedDate = LocalDate.now(),   // <-- Added this line
            onEventClick = onEventNavigate,
            modifier = modifier
        )

        CalendarMode.WEEKLY -> WeeklyView(
            context = context,
            calendarViewModel = calendarViewModel,
            uiState = uiState,
            onEventClick = onEventNavigate,
            modifier = modifier
        )

        CalendarMode.MONTHLY -> MonthlyView(
            context = context,
            calendarViewModel = calendarViewModel,
            uiState = uiState,
            onEventClick = onEventNavigate,
            modifier = modifier
        )
    }

    Log.d("CalendarScreen", "Recomposed: isLoading=${uiState.isLoading}, events=${uiState.allEvents.size}")
}
