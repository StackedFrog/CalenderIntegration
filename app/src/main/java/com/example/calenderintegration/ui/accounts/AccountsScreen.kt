@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.calenderintegration.ui.accounts

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.calenderintegration.ui.auth.AuthViewModel
import com.example.calenderintegration.ui.components.ConfirmDialog
import com.example.calenderintegration.ui.components.ProviderPickerDialog

@Composable
fun AccountsScreen(
    accountsViewModel: AccountsViewModel = viewModel(),
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by accountsViewModel.accountsState.collectAsState()
    val context = LocalContext.current

    // local dialog state for “enter Zoho email”
    var showZohoEmailDialog by remember { mutableStateOf(false) }
    var zohoEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { accountsViewModel.loadAllAccounts(context) }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            accountsViewModel.loadAllAccounts(context)
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text("Accounts List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { accountsViewModel.openProviderPicker() }) {
                        Icon(Icons.Filled.Person, contentDescription = "Add Account")
                    }
                }
            )

            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (state.accounts.isEmpty()) {
                EmptyRow(hint = "No accounts yet")
            } else {
                LazyColumn {
                    items(state.accounts) { acc ->
                        AccountRow(
                            primary = acc.provider,
                            secondary = acc.email,
                            onClick = { accountsViewModel.requestDelete(acc.email) }
                        )
                        HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { accountsViewModel.openProviderPicker() }) {
                    Text("Add Account")
                }
            }
        }
    }

    // Delete confirm
    ConfirmDialog(
        visible = state.showDeleteDialog,
        title = "Delete account",
        message = "Are you sure you want to delete ${state.pendingDeleteEmail}?",
        onConfirm = { accountsViewModel.confirmDelete(context) },
        onCancel = { accountsViewModel.cancelDelete() }
    )

    // Provider picker (Google + Zoho)
    ProviderPickerDialog(
        visible = state.showProviderPicker,
        onPickGoogle = {
            accountsViewModel.dismissProviderPicker()
            authViewModel.logIn(
                context = context,
                startIntentSender = { req: IntentSenderRequest ->
                    intentSenderLauncher.launch(req)
                }
            )
        },
        onPickZoho = {
            accountsViewModel.dismissProviderPicker()
            showZohoEmailDialog = true              // <-- open email prompt
        },
        onDismiss = { accountsViewModel.dismissProviderPicker() }
    )

    // Simple email input for Zoho
    if (showZohoEmailDialog) {
        AlertDialog(
            onDismissRequest = { showZohoEmailDialog = false },
            title = { Text("Add Zoho account") },
            text = {
                OutlinedTextField(
                    value = zohoEmail,
                    onValueChange = { zohoEmail = it },
                    singleLine = true,
                    label = { Text("Email") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (zohoEmail.isNotBlank()) {
                            accountsViewModel.addZohoByEmail(context, zohoEmail)
                        }
                        showZohoEmailDialog = false
                        zohoEmail = ""
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showZohoEmailDialog = false
                        zohoEmail = ""
                    }
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AccountRow(
    primary: String,
    secondary: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = primary,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyRow(hint: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
