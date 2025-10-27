package com.example.calenderintegration.ui.calendar



import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    context: Context,
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // Start of current week (Monday-based)
    val startOfWeek = remember { LocalDate.now().with(java.time.DayOfWeek.MONDAY) }
    val daysOfWeek = remember { (0..6).map { startOfWeek.plusDays(it.toLong()) } }
    val horizontalScroll = rememberScrollState()

    // Fetch events dynamically from ViewModel
    val weeklyEvents = remember(uiState.allEvents) {
        calendarViewModel.getEventsForWeek(LocalDate.now())
    }

    Row(
        modifier = modifier
            .horizontalScroll(horizontalScroll)
            .padding(8.dp)
    ) {
        daysOfWeek.forEach { date ->
            val eventsForDay = remember(date, weeklyEvents) {
                calendarViewModel.getEventsForDay(date)
            }

            DayColumn(
                date = date,
                events = eventsForDay,
                onEventClick = onEventClick,
                width = 140.dp
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
                .background(color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center,

        ) {
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                ),
                textAlign = TextAlign.Center
            )
        }

        // Events list for this day
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 8.dp)
        ) {
            items(events) { event ->
                EventCard(event = event, onClick = { onEventClick(event) })
            }

            if (events.isEmpty()) {
                item {
                    Text(
                        text = "No events",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        textAlign = TextAlign.Center
                    )
                }
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
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = event.summary,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            // These values are already parsed in CalendarRepository
            Text(
                text = "${event.start} - ${event.end}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (event.location.isNotBlank()) {
                Text(
                    text = "📍 ${event.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}




