package com.example.calenderintegration.ui.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calenderintegration.model.Event

@Composable
fun MonthlyView(
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (event: Event) -> Unit,
    modifier: Modifier = Modifier
){
    Text("Hello from monthly view!")

}
