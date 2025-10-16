package com.example.calenderintegration.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calenderintegration.model.Event

@Composable
fun CalendarEventList(events: List<Event>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(events) { event ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = event.summary, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${event.start} → ${event.end}", style = MaterialTheme.typography.bodyMedium)
                    if (event.location.isNotEmpty()) {
                        Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}