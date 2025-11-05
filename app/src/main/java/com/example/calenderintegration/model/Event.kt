package com.example.calenderintegration.model


/**
 *
 * Pablo 24/10: Changed 'val' to 'var' so we can update the events
 */
data class Event( //Needs to be as basic as possible when it comes to the structure of an event, have your apiEvent conversion follow this
    var id: String,
    var summary: String,
    var description: String,
    val location: String,
    val start: String,
    val end: String,
    val calendarEmail: String,
    val calendarId: String? = null,   // MUST exist
    val isEditable: Boolean = true    // used by UI and repo guards
)


