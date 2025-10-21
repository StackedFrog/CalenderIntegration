package com.example.calenderintegration.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calenderintegration.ui.accounts.AccountsScreen
import com.example.calenderintegration.ui.accounts.AccountsViewModel
import com.example.calenderintegration.ui.auth.AuthViewModel
import com.example.calenderintegration.ui.auth.LoginScreen
import com.example.calenderintegration.ui.calendar.CalendarMode
import com.example.calenderintegration.ui.calendar.CalendarScreen
import com.example.calenderintegration.ui.calendar.CalendarViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val navHistory = remember { mutableStateListOf<String>() }

    // viewModels for all screens
    val calendarViewModel: CalendarViewModel = viewModel()
    val accountsViewModel: AccountsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val isLoggedIn: Boolean = authViewModel.isLoggedIn()

    // Decide start destination based on login state
    val startDestination = if (isLoggedIn) "weeklyCalendar" else "login"

    // Navigation host handles screen navigation

    val currentDestination = navBackStackEntry.value?.destination?.route

    // top bar and bottom bar
    Scaffold(
        bottomBar =  {
            // only show bottom bar if user is logged in and not on the login page
            if (isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                NavigationBar (windowInsets = NavigationBarDefaults.windowInsets)
                {
                    NavigationBarItem(
                        selected = currentDestination == "dailyCalendar",
                        onClick = { navigateWithHistory(navController, navHistory, "dailyCalendar") },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Daily") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "weeklyCalendar",
                        onClick = { navigateWithHistory(navController, navHistory, "weeklyCalendar") },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        label = { Text("Weekly") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "monthlyCalendar",
                        onClick = { navigateWithHistory(navController, navHistory, "monthlyCalendar") },
                        icon = { Icon(Icons.Default.Build, contentDescription = null) },
                        label = { Text("Monthly") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable("dailyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.DAILY
                    )
                }
                composable("weeklyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.WEEKLY
                    )
                }
                composable("monthlyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.MONTHLY
                    )
                }
                composable("accounts") {
                    AccountsScreen(accountsViewModel, navController)
                }
                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            navigateWithHistory(navController, navHistory, "weeklyCalendar")
                        }
                    )
                }
            }
            if (isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                FloatingAccountButton(
                    { navigateWithHistory(navController, navHistory, "accounts")},
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}