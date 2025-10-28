package com.example.calenderintegration.ui.eventView

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.repository.EventRepository
import com.example.calenderintegration.repository.AccountsRepository

import com.example.calenderintegration.api.googleapi.CalendarApiService
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EventView(
    eventId: String?,
    viewModel: EventViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    val state by viewModel.eventState.collectAsState()

    var summary by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var calendarEmail by remember { mutableStateOf("") }

    // --- Move this here ---
    @Composable
    fun labeledField(label: String, value: String, onValueChange: (String) -> Unit) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .heightIn(min = 40.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                .padding(8.dp),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
        )
        Spacer(Modifier.height(8.dp))
    }
    // ---

    LaunchedEffect(eventId) {
        if (eventId != null) viewModel.loadEvent(context, eventId)
    }

    LaunchedEffect(state.event) {
        state.event?.let { e ->
            summary = e.summary
            description = e.description
            location = e.location
            start = e.start
            end = e.end
            calendarEmail = e.calendarEmail
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Event Editor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (state.error != null) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        labeledField("Summary", summary) { summary = it }
        labeledField("Description", description) { description = it }
        labeledField("Location", location) { location = it }
        labeledField("Start", start) { start = it }
        labeledField("End", end) { end = it }
        labeledField("Calendar Email", calendarEmail) { calendarEmail = it }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val updated = state.event?.copy(
                    summary = summary,
                    description = description,
                    location = location,
                    start = start,
                    end = end,
                    calendarEmail = calendarEmail
                )

                if (updated != null) {
                    viewModel.updateEvent(context, updated)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save Changes to Google")
        }
    }
}

