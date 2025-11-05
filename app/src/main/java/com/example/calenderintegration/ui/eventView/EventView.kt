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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.calenderintegration.ui.accounts.AccountsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

    // --- Changed from raw strings to parsed values ---
    var startDate by remember(eventId) { mutableStateOf<LocalDate?>(null) }
    var startTime by remember(eventId) { mutableStateOf<LocalTime?>(null) }
    var endDate by remember(eventId) { mutableStateOf<LocalDate?>(null) }
    var endTime by remember(eventId) { mutableStateOf<LocalTime?>(null) }

    var selectedEmail by remember(eventId) { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

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
            startDate = null
            startTime = null
            endDate = null
            endTime = null
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
            selectedEmail = e.calendarEmail

            // Parse and format start/end for display
            try {
                if (e.start.contains("T")) {
                    val parsed = OffsetDateTime.parse(e.start)
                    startDate = parsed.toLocalDate()
                    startTime = parsed.toLocalTime()
                }
                if (e.end.contains("T")) {
                    val parsed = OffsetDateTime.parse(e.end)
                    endDate = parsed.toLocalDate()
                    endTime = parsed.toLocalTime()
                }
            } catch (_: Exception) {}
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

        // --- Replace raw Start/End text inputs ---
        Text("Start", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showStartDatePicker = true }) {
                Text(startDate?.toString() ?: "Select Date")
            }
            Button(onClick = { showStartTimePicker = true }) {
                Text(startTime?.toString() ?: "Select Time")
            }
        }

        Spacer(Modifier.height(8.dp))

        Text("End", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showEndDatePicker = true }) {
                Text(endDate?.toString() ?: "Select Date")
            }
            Button(onClick = { showEndTimePicker = true }) {
                Text(endTime?.toString() ?: "Select Time")
            }
        }

        Spacer(Modifier.height(16.dp))

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
                // Convert date/time into ISO 8601 format
                val start = if (startDate != null && startTime != null)
                    ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                else ""

                val end = if (endDate != null && endTime != null)
                    ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                else ""

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
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                if (success) "Event created" else "Failed to create event",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (success) navController.popBackStack("dailyCalendar", false)
                        }
                    }
                } else {
                    viewModel.updateEvent(context, updated) { success ->
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                if (success) "Event updated" else "Failed to update event",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (success) navController.popBackStack("dailyCalendar", false)
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save Event")
        }
    }

    // --- Material Pickers ---
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateChange = { startDate = it; showStartDatePicker = false }
        )
    }
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeChange = { startTime = it; showStartTimePicker = false }
        )
    }
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateChange = { endDate = it; showEndDatePicker = false }
        )
    }
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeChange = { endTime = it; showEndTimePicker = false }
        )
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
@Composable
fun DatePickerDialog(onDismissRequest: () -> Unit, onDateChange: (LocalDate) -> Unit) {
    val datePickerState = rememberDatePickerState()
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateChange(
                        Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault()).toLocalDate())
                }
            }) { Text("OK") }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}

@Composable
fun TimePickerDialog(onDismissRequest: () -> Unit, onTimeChange: (LocalTime) -> Unit) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val dialog = android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimeChange(LocalTime.of(hour, minute))
                onDismissRequest()
            },
            LocalTime.now().hour,
            LocalTime.now().minute,
            true
        )
        dialog.show()

        onDispose { dialog.dismiss() }
    }
}

