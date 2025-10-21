package com.example.calenderintegration.model

data class GoogleAccount(
    val email: String,
    val displayName: String,
    val idToken: String? = null,        // identity verification
    val accessToken: String,            // API access
    val calendars: List<GoogleCalendar> = emptyList()
)
data class GoogleCalendar(//not used in the app yet or ever
    val id: String,
    val summary: String,
    val description: String,
    val timeZone: String,
    val events: List<Event> = emptyList()
)
