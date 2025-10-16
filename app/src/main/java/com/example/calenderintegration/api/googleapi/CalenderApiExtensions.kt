package com.example.calenderintegration.api.googleapi


import com.example.calenderintegration.model.CalendarType
import com.example.calenderintegration.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Event as ApiEvent

/**
 * Converts a Google Calendar API Event object into your app's Event data class.
 */
fun ApiEvent.toAppEvent(): Event {
    return Event(
        id = this.id ?: "",
        summary = this.summary ?: "",
        description = this.description ?: "",
        start = this.start?.dateTime?.toString() ?: this.start?.date?.toString() ?: "",
        end = this.end?.dateTime?.toString() ?: this.end?.date?.toString() ?: "",
        location = this.location ?: "",
        calendarType = CalendarType.GOOGLE
    )
}

/**
 * Converts a list of API events to a list of app Event models.
 */
fun List<ApiEvent>.toAppEvents(): List<Event> = this.map { it.toAppEvent() }







fun Event.toApiEvent(): ApiEvent {
    val apiEvent = ApiEvent()
        .setId(this.id.ifEmpty { null }) // Google may generate an ID if null
        .setSummary(this.summary)
        .setDescription(this.description)
        .setLocation(this.location)

    // Set start and end times
    apiEvent.start = EventDateTime().setDateTime(com.google.api.client.util.DateTime(this.start))
    apiEvent.end = EventDateTime().setDateTime(com.google.api.client.util.DateTime(this.end))




    return apiEvent
}