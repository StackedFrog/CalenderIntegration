package com.example.calenderintegration.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ProviderPickerDialog(
    visible: Boolean,
    onPickGoogle: () -> Unit,
    onPickZoho: () -> Unit,            // <-- now active
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose account type") },
        text = {},
        confirmButton = {
            Button(onClick = onPickGoogle) {
                Text("Google")
            }
        },
        dismissButton = {
            Button(onClick = onPickZoho) { // <-- clickable (no “coming soon”)
                Text("Zoho")
            }
        }
    )
}
