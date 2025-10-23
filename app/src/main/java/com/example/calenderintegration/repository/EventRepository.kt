package com.example.calenderintegration.repository

import com.example.calenderintegration.model.Event
import jakarta.inject.Inject

// Inject a constructor to make hilt automatically add dependencies
class EventRepository @Inject constructor(){
    fun getEventById(id: String): Event? {
        // get an event by id
        return null
    }

    fun updateEvent(event: Event): Event? {
        // update event with the same id
        // to new description, summary
        return null
    }
}
