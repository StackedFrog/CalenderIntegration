package com.example.calenderintegration.ui.calendar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calenderintegration.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun MonthlyView(
    context: Context,
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (event: Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { LocalDate.now().withDayOfMonth(1) }
    val today = LocalDate.now()
    val daysInMonth = currentMonth.lengthOfMonth()
    val days = remember { (1..daysInMonth).map { currentMonth.withDayOfMonth(it) } }

    val monthEvents = remember(uiState.allEvents) {
        calendarViewModel.getEventsForMonth(LocalDate.now())
    }

    val selectedDay = remember { mutableStateOf<LocalDate?>(null) }

    val daysOfWeek = DayOfWeek.values()
        .toList()
        .map { it.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Month and year title
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF0D47A1),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday headers (Sun, Mon, ...)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName.uppercase(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(days) { day ->
                val eventsForDay = remember(day, monthEvents) {
                    calendarViewModel.getEventsForDay(day)
                }

                val isToday = day == today
                val isSelected = day == selectedDay.value

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            when {
                                isSelected -> Color(0xFFBBDEFB)
                                isToday -> Color(0xFF90CAF9)
                                else -> Color(0xFFE3F2FD)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedDay.value = day }
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isToday) Color(0xFF0D47A1) else Color.Black
                        )

                        if (eventsForDay.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .background(Color(0xFF1976D2), shape = RoundedCornerShape(50))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = eventsForDay.size.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Events list for the selected day
        selectedDay.value?.let { day ->
            val events = calendarViewModel.getEventsForDay(day)
            Text(
                text = "Events on ${day.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0D47A1),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (events.isEmpty()) {
                Text("No events", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(events) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEventClick(event) },
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    event.summary,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF0D47A1)
                                )
                                if (event.description.isNotBlank()) {
                                    Text(
                                        event.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray
                                    )
                                }
                                Text(
                                    "${event.start} - ${event.end}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
