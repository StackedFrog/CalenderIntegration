package com.example.calenderintegration.ui.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calenderintegration.model.Event
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyView(
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (event: Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by calendarViewModel.uiState.collectAsState()

    // Hardcoded events grouped by date
    val eventsByDate = mapOf(
        "2025-10-24" to listOf(
            Event(
                id = "1",
                summary = "Morning Meeting",
                description = "Project updates with team",
                start = "09:00",
                end = "10:00",
                calendarEmail = "work@example.com"
            ),
            Event(
                id = "2",
                summary = "Code Review",
                description = "Review latest commits",
                start = "11:00",
                end = "11:30",
                calendarEmail = "work@example.com"
            ),


            Event(
                id = "3",
                summary = "Design Session",
                description = "UI redesign discussion",
                start = "13:00",
                end = "14:30",
                calendarEmail = "work@example.com"
            ),
            Event(
                id = "4",
                summary = "Client Call",
                description = "Discuss new requirements",
                start = "15:00",
                end = "16:00",
                calendarEmail = "client@example.com"
            ),
            Event(
            id = "3",
            summary = "Design Session",
            description = "UI redesign discussion",
            start = "13:00",
            end = "14:30",
            calendarEmail = "work@example.com"
        ),
        Event(
            id = "4",
            summary = "Client Call",
            description = "Discuss new requirements",
            start = "15:00",
            end = "16:00",
            calendarEmail = "client@example.com"
        )

        )
    )

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center // Centers the colored box on the screen
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(0.75f)
                .background(
                    color = Color(0xFFE3F2FD), // light blue wrapper box
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                eventsByDate.forEach { (date, events) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0D47A1),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(events) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEventClick(event) },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(event.summary, style = MaterialTheme.typography.titleMedium)
                                if (event.description.isNotBlank()) {
                                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    "${event.start} - ${event.end}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (event.location.isNotBlank()) {
                                    Text(
                                        "📍 ${event.location}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom row container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp) // increase total height of button area
                .background(Color(0xFFE3F2FD))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { /* TODO: action 1 */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()// make button fill row height
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Today", fontSize = 18.sp)
            }
            Button(
                onClick = { /* TODO: action 2 */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()// make button fill row height
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Week", fontSize = 18.sp)
            }
            Button(
                onClick = { /* TODO: action 3 */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()// make button fill row height
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Month", fontSize = 18.sp)
            }
        }
    }

}






