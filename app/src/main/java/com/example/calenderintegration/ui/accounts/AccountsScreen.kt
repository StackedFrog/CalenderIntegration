package com.example.calenderintegration.ui.accounts

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun AccountsScreen(
    accountsViewModel: AccountsViewModel = viewModel(),
    navController: NavHostController,
    modifier: Modifier = Modifier,
    ) {
    val accountsState by accountsViewModel.accountsState.collectAsState()

    accountsViewModel.loadAllAccounts()

    val googleAccounts = accountsState.googleAccounts
    val outlookAccounts = accountsState.outlookAccounts

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Adds padding around the screen
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centers content vertically
    ) {
        Text(
            text = "Hello from Accounts screen!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 20.dp) // Adds space below the Text
        )

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp) // Adds space above the button
        ) {
            Text("Go Back")
        }

    }

}