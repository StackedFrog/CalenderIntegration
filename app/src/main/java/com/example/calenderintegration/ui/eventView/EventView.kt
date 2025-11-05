package com.example.calenderintegration.ui.eventView

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import androidx.navigation.NavController
import com.example.calenderintegration.ui.accounts.AccountsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventView(
    eventId: String?,
    viewModel: EventViewModel = hiltViewModel(),
    accountsViewModel: AccountsViewModel = hiltViewModel(),
    navController: NavController,
    context: Context = LocalContext.current
) {
    val state by viewModel.eventState.collectAsState()
    val accountsState by accountsViewModel.accountsState.collectAsState()

    var summary by remember(eventId) { mutableStateOf("") }
    var description by remember(eventId) { mutableStateOf("") }
    var location by remember(eventId) { mutableStateOf("") }
    var start by remember(eventId) { mutableStateOf("") }
    var end by remember(eventId) { mutableStateOf("") }
    var selectedEmail by remember(eventId) { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Load accounts once
    LaunchedEffect(Unit) {
        accountsViewModel.loadAllAccounts(context)
    }

    // Load or reset event data
    LaunchedEffect(eventId) {
        if (eventId.isNullOrEmpty()) {
            viewModel.resetEventState()
            summary = ""
            description = ""
            location = ""
            start = ""
            end = ""
            selectedEmail = ""
        } else {
            viewModel.loadEvent(context, eventId)
        }
    }

    // When event loads, update UI fields
    LaunchedEffect(state.event) {
        state.event?.let { e ->
            summary = e.summary
            description = e.description
            location = e.location
            start = e.start
            end = e.end
            selectedEmail = e.calendarEmail
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Event Editor", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        state.error?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        labeledField("Summary", summary) { summary = it }
        labeledField("Description", description) { description = it }
        labeledField("Location", location) { location = it }
        labeledField("Start", start) { start = it }
        labeledField("End", end) { end = it }

        Text("Select Calendar Account", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedEmail,
                onValueChange = {},
                label = { Text("Account") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accountsState.accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.email) },
                        onClick = {
                            selectedEmail = account.email
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val updated = state.event?.copy(
                    summary = summary,
                    description = description,
                    location = location,
                    start = start,
                    end = end,
                    calendarEmail = selectedEmail
                ) ?: viewModel.createEmptyEvent().copy(
                    summary = summary,
                    description = description,
                    location = location,
                    start = start,
                    end = end,
                    calendarEmail = selectedEmail
                )

                val isNew = updated.id.isBlank()

                if (isNew) {
                    viewModel.createEvent(context, updated) { success ->
                        // Switch to main thread for UI actions
                        Handler(Looper.getMainLooper()).post {
                            if (success) {
                                Toast.makeText(context, "Event created", Toast.LENGTH_SHORT).show()
                                navController.popBackStack("dailyCalendar", inclusive = false)
                            } else {
                                Toast.makeText(context, "Failed to create event", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    viewModel.updateEvent(context, updated) { success ->
                        Handler(Looper.getMainLooper()).post {
                            if (success) {
                                Toast.makeText(context, "Event updated", Toast.LENGTH_SHORT).show()
                                navController.popBackStack("dailyCalendar", inclusive = false)
                            } else {
                                Toast.makeText(context, "Failed to update event", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save Event")
        }
    }
}

@Composable
private fun labeledField(label: String, value: String, onValueChange: (String) -> Unit) {
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
