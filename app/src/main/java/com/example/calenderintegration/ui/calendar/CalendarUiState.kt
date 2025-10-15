package com.example.calenderintegration.ui.calendar

import com.example.calenderintegration.model.Event

data class CalendarUiState (
    val currentMode: CalendarMode = CalendarMode.WEEKLY,
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class CalendarMode { DAILY, WEEKLY, MONTHLY}