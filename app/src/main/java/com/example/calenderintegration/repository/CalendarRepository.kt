package com.example.calenderintegration.repository

import com.example.calenderintegration.model.Event
import jakarta.inject.Inject

// Inject a constructor to make hilt automatically add dependencies
class CalendarRepository @Inject constructor(){
    suspend fun loadAllEvents(): List<Event> {
        // TODO
        return emptyList()
    }
}