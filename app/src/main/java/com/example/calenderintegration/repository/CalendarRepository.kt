package com.example.calenderintegration.repository

import com.example.calenderintegration.model.Event
import jakarta.inject.Inject

// Inject a constructor so that hilt doesn't fuck up
class CalendarRepository @Inject constructor(){
    suspend fun loadAllEvents(): List<Event> {
        // TODO
        return emptyList()
    }
}