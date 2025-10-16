package com.example.calenderintegration.model


enum class CalendarType {
    GOOGLE
    // Later you can add OUTLOOK if needed
}


data class Event( //Needs to be as basic as possible when it comes to the structure of an event, have your apiEvent conversion follow this
    val id: String,
    val summary: String,
    val description: String,
    val start: String,
    val end: String,
    val location: String = "",
    val calendarType: CalendarType = CalendarType.GOOGLE
)