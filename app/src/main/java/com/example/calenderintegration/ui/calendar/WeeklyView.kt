package com.example.calenderintegration.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calenderintegration.model.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun WeeklyView(
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayColumnWidth = 140.dp

    // Start of the current week (Monday-based)
    val startOfWeek = remember { LocalDate.now().with(java.time.DayOfWeek.MONDAY) }
    val daysOfWeek = remember { (0..6).map { startOfWeek.plusDays(it.toLong()) } }
    val horizontalScroll = rememberScrollState()

    ///TESTESTEST
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val sampleEvents = listOf(
        Event(
            id = "1",
            summary = "Team Standup",
            description = "Daily sync with team",
            start = LocalDateTime.now().withHour(9).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().withHour(9).withMinute(30).format(isoFormatter),
            location = "Conference Room A",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "2",
            summary = "Client Meeting",
            description = "Discuss project deliverables",
            start = LocalDateTime.now().withHour(11).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().withHour(12).withMinute(0).format(isoFormatter),
            location = "Zoom",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "3",
            summary = "Lunch with Sarah",
            description = "Catch up at cafe",
            start = LocalDateTime.now().withHour(13).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().withHour(14).withMinute(0).format(isoFormatter),
            location = "Downtown Cafe",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "4",
            summary = "Gym",
            description = "Workout session",
            start = LocalDateTime.now().withHour(18).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().withHour(19).withMinute(5).format(isoFormatter),
            location = "Fitness Center",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "18",
            summary = "Gym",
            description = "Workout session",
            start = LocalDateTime.now().withHour(19).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().withHour(20).withMinute(0).format(isoFormatter),
            location = "Fitness Center",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "19",
            summary = "Gym",
            description = "Workout session",
            start = LocalDateTime.now().withHour(20).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().withHour(22).withMinute(0).format(isoFormatter),
            location = "Fitness Center",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "5",
            summary = "Project Brainstorm",
            description = "Creative ideas for upcoming release",
            start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(1).withHour(11).withMinute(30).format(isoFormatter),
            location = "Room B",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "6",
            summary = "Doctor Appointment",
            description = "Routine check-up",
            start = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(1).withHour(15).withMinute(30).format(isoFormatter),
            location = "Clinic",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "7",
            summary = "Weekly Sync",
            description = "Product and design alignment",
            start = LocalDateTime.now().plusDays(2).withHour(9).withMinute(30).format(isoFormatter),
            end = LocalDateTime.now().plusDays(2).withHour(10).withMinute(30).format(isoFormatter),
            location = "Online Meeting",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "8",
            summary = "Lunch & Learn",
            description = "New tech presentation",
            start = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(2).withHour(13).withMinute(0).format(isoFormatter),
            location = "Cafeteria",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "9",
            summary = "Coffee with John",
            description = "Networking meeting",
            start = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(3).withHour(10).withMinute(30).format(isoFormatter),
            location = "Coffee Shop",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "10",
            summary = "Yoga Class",
            description = "Evening relaxation session",
            start = LocalDateTime.now().plusDays(3).withHour(18).withMinute(30).format(isoFormatter),
            end = LocalDateTime.now().plusDays(3).withHour(19).withMinute(30).format(isoFormatter),
            location = "Yoga Studio",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "11",
            summary = "Product Demo",
            description = "Present features to stakeholders",
            start = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(4).withHour(12).withMinute(0).format(isoFormatter),
            location = "Boardroom",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "12",
            summary = "Grocery Shopping",
            description = "Weekly groceries",
            start = LocalDateTime.now().plusDays(4).withHour(17).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(4).withHour(18).withMinute(0).format(isoFormatter),
            location = "Supermarket",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "13",
            summary = "Sprint Retrospective",
            description = "Team reflection on sprint",
            start = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).format(isoFormatter),
            location = "Zoom",
            calendarEmail = "work@calendar.com"
        ),
        Event(
            id = "14",
            summary = "Dinner with Family",
            description = "Weekend dinner",
            start = LocalDateTime.now().plusDays(5).withHour(19).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(5).withHour(21).withMinute(0).format(isoFormatter),
            location = "Home",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "15",
            summary = "Weekend Hike",
            description = "Trail with friends",
            start = LocalDateTime.now().plusDays(6).withHour(8).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(6).withHour(11).withMinute(0).format(isoFormatter),
            location = "Mountain Trail",
            calendarEmail = "personal@calendar.com"
        ),
        Event(
            id = "16",
            summary = "Brunch",
            description = "Lazy Sunday brunch",
            start = LocalDateTime.now().plusDays(6).withHour(12).withMinute(0).format(isoFormatter),
            end = LocalDateTime.now().plusDays(6).withHour(13).withMinute(0).format(isoFormatter),
            location = "Downtown Cafe",
            calendarEmail = "personal@calendar.com"
        )
    )

    Row(
        modifier = modifier
            .horizontalScroll(horizontalScroll)
            .padding(8.dp)
    ) {
        daysOfWeek.forEach { date ->
            DayColumn(
                date = date,
                events = sampleEvents.filter { it.isOnDate(date) },
                onEventClick = onEventClick,
                width = dayColumnWidth
            )
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    width: Dp
) {
    val formatter = DateTimeFormatter.ofPattern("EEE\ndd")
    Column(
        modifier = Modifier
            .width(width)
            .padding(4.dp)
    ) {
        // Day header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
        }

        // Events list
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 8.dp)
        ) {
            items(events.size) { idx ->
                val event = events[idx]
                EventCard(event = event, onClick = { onEventClick(event) })
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = event.summary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = formatEventTime(event.start, event.end),
                fontSize = 12.sp,
                color = Color.Gray
            )
            if (event.location.isNotEmpty()) {
                Text(
                    text = event.location,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
private fun Event.isOnDate(date: LocalDate): Boolean {
    // Assumes ISO 8601 start time (e.g., "2025-10-23T09:00:00Z")
    return try {
        val eventDate = LocalDate.parse(start.substring(0, 10))
        eventDate.isEqual(date)
    } catch (e: Exception) {
        false
    }
}

private fun formatEventTime(start: String, end: String): String {
    return try {
        val startTime = start.substring(11, 16)
        val endTime = end.substring(11, 16)
        "$startTime - $endTime"
    } catch (e: Exception) {
        "All Day"
    }
}
