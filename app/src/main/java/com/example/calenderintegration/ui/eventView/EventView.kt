package com.example.calenderintegration.ui.eventView

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun EventView(
    eventId: String?,
    eventViewModel: EventViewModel,
) {
    // make function inside eventViewModel to get event by id

    val eventState by eventViewModel.eventState.collectAsState()

    Text("Hello from eventview!")

}