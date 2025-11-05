package com.example.calenderintegration.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProviderPickerDialog(
    visible: Boolean,
    onPickGoogle: () -> Unit,
    onPickZoho: () -> Unit, // stub for later
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose account type") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Button(
                    onClick = onPickGoogle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) { Text("Google") }

                OutlinedButton(
                    onClick = onPickZoho,
                    enabled = false, // Not implemented yet
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) { Text("Zoho (coming soon)") }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
