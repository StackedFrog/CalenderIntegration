package com.example.calenderintegration.ui.eventView

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.calenderintegration.model.Event

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

    @Composable
    fun labeledField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        enabled: Boolean,
        hint: String? = null,
        maskedDateTime: Boolean = false
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        if (enabled) {
            BasicTextField(
                value = value,
                onValueChange = { new ->
                    if (maskedDateTime) onValueChange(maskDateTime(new))
                    else onValueChange(new)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .heightIn(min = 40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .padding(8.dp),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
            )
            if (!hint.isNullOrBlank()) {
                Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value.ifBlank { "—" },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    val isNew = state.event?.id?.isBlank() == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            if (state.isEditing) (if (isNew) "Create Event" else "Edit Event") else "Event",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))

        state.error?.let { err ->
            Text("Error: $err", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (state.isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Loading event…")
            }
            return@Column
        }

        labeledField("Title", summary, { summary = it }, enabled = state.isEditing)
        labeledField("Description", description, { description = it }, enabled = state.isEditing)
        labeledField("Location", location, { location = it }, enabled = state.isEditing)

        // Masked datetime fields: yyyy-MM-dd HH:mm
        labeledField(
            label = "Start (yyyy-MM-dd HH:mm or yyyy-MM-dd)",
            value = start,
            onValueChange = { start = it },
            enabled = state.isEditing,
            hint = "Type date first. The '-', space and ':' auto-insert.",
            maskedDateTime = true
        )
        labeledField(
            label = "End (yyyy-MM-dd HH:mm or yyyy-MM-dd)",
            value = end,
            onValueChange = { end = it },
            enabled = state.isEditing,
            hint = "Leave just yyyy-MM-dd for all-day.",
            maskedDateTime = true
        )

        labeledField("Calendar Email", calendarEmail, { calendarEmail = it }, enabled = state.isEditing)

        Spacer(Modifier.height(16.dp))

        if (!state.isEditing) {
            Button(
                onClick = { viewModel.startEditing() },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = state.event != null && !state.isSaving
            ) { Text(if (isNew) "Create" else "Modify Event") }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val base: Event = state.event ?: return@Button
                        val updated = base.copy(
                            summary = summary.trim(),
                            description = description.trim(),
                            location = location.trim(),
                            start = start.trim(),
                            end = end.trim(),
                            calendarEmail = calendarEmail.trim()
                        )
                        viewModel.confirmEdit(context, updated)
                    },
                    enabled = state.event != null && !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp)
                        )
                    }
                    Text(if (isNew) "Create" else "Save")
                }

                OutlinedButton(
                    onClick = { viewModel.cancelEditing() },
                    enabled = !state.isSaving
                ) { Text("Cancel") }
            }
        }
    }
}

/**
 * Simple live mask for "yyyy-MM-dd HH:mm".
 * Auto-inserts '-' at 4 & 7, ' ' at 10, ':' at 13. Limits to 16 chars.
 * Also allows stopping at 10 chars (all-day date).
 */
private fun maskDateTime(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    val sb = StringBuilder()
    for (i in digits.indices) {
        val c = digits[i]
        sb.append(c)
        when (i) {
            3 -> sb.append('-')         // after yyyy
            5 -> sb.append('-')         // after yyyy-MM
            7 -> sb.append(' ')         // after yyyy-MM-dd
            9 -> sb.append(':')         // after yyyy-MM-dd HH
        }
        // Stop at "yyyy-MM-dd HH:mm" -> digits count 12 -> total length 16
        if (i >= 11) break
    }
    return sb.toString()
}
