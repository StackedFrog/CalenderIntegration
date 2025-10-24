package com.example.calenderintegration.model





data class Event( //Needs to be as basic as possible when it comes to the structure of an event, have your apiEvent conversion follow this
    val id: String,
    var summary: String,
    var description: String,
    var start: String,
    var end: String,
    var location: String = "",
    val calendarEmail: String
)


