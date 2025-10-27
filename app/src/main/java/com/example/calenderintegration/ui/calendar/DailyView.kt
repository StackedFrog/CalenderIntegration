package com.example.calenderintegration.ui.calendar

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.calenderintegration.model.Event
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp

import java.time.LocalDate

@Composable
fun DailyView(
    context: Context,
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    selectedDate: LocalDate,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use the ViewModel to dynamically compute events for the selected date
    val dailyEvents = remember(selectedDate, uiState.allEvents) {
        calendarViewModel.getEventsForDay(selectedDate)
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.75f)
            .background(
                color = MaterialTheme.colorScheme.background,
            )
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                Text("Loading events...", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.align(Alignment.Center))
            }

            dailyEvents.isEmpty() -> {
                Text(
                    "No events for ${selectedDate}",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Events for $selectedDate",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(dailyEvents) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEventClick(event) },
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
                                    event.summary,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (event.description.isNotBlank()) {
                                    Text(
                                        event.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    "${event.start} - ${event.end}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (event.location.isNotBlank()) {
                                    Text(
                                        "📍 ${event.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

