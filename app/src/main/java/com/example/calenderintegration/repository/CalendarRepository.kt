package com.example.calenderintegration.repository

import com.example.calenderintegration.model.Event
import jakarta.inject.Inject
import java.time.LocalDate

// Inject a constructor to make hilt automatically add dependencies
class CalendarRepository @Inject constructor(){
    fun getEventsByMonth(date: LocalDate): List<Event> {
        // strip month
        // return a list of all events of all logged in users for that month
        return emptyList()
    }

    fun getEventsByWeek(date: LocalDate): List<Event> {
        // strip week
        // return a list of all events of all logged in users for that week
        return emptyList()
    }

    fun getEventsByDay(date: LocalDate): List<Event> {
        // strip day
        // return a list of all events of all logged in users for that day
        return emptyList()
    }
}