package com.example.calenderintegration.ui.calendar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement

@Composable
fun MonthlyView(
    context: Context,
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (event: Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { LocalDate.now().withDayOfMonth(1) }
    val daysInMonth = remember { currentMonth.lengthOfMonth() }
    val days = remember { (1..daysInMonth).map { currentMonth.withDayOfMonth(it) } }

    val monthEvents = remember(uiState.allEvents) {
        calendarViewModel.getEventsForMonth(LocalDate.now())
    }

    val selectedDay = remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF0D47A1),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar grid (7 columns)
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

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            if (day == selectedDay.value) Color(0xFFBBDEFB)
                            else Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedDay.value = day }
                        .padding(6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (eventsForDay.isNotEmpty()) {
                            Text(
                                text = "${eventsForDay.size} events",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
