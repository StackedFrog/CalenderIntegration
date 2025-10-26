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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.calenderintegration.ui.auth.AuthViewModel
import java.time.LocalDate

@Composable
fun DailyView(
    calendarViewModel: CalendarViewModel,
    uiState: CalendarUiState,
    onEventClick: (event: Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyEvents = uiState.dailyEvents
    val context = LocalContext.current



        // --- Centered rounded event box ---
        Box(
            modifier = Modifier

                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f)
                .background(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Text(
                    "Loading events...",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Events for ${LocalDate.now()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0D47A1),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(dailyEvents) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEventClick(event) },
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
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
                                if (event.location.isNotBlank()) {
                                    Text(
                                        "📍 ${event.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    if (dailyEvents.isEmpty()) {
                        item {
                            Text(
                                text = "No events today",
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }










