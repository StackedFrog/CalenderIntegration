package com.example.calenderintegration.ui.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calenderintegration.ui.accounts.AccountsScreen
import com.example.calenderintegration.ui.accounts.AccountsViewModel
import com.example.calenderintegration.ui.auth.AuthViewModel
import com.example.calenderintegration.ui.auth.LoginScreen
import com.example.calenderintegration.ui.calendar.CalendarMode
import com.example.calenderintegration.ui.calendar.CalendarScreen
import com.example.calenderintegration.ui.calendar.CalendarViewModel
import com.example.calenderintegration.ui.eventView.EventView
import com.example.calenderintegration.ui.eventView.EventViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(context: Context = LocalContext.current) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val navHistory = remember { mutableStateListOf<String>() }

    // viewModels for all screens
    val calendarViewModel: CalendarViewModel = viewModel()
    val accountsViewModel: AccountsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel()

    // Collect UI states
    val uiState by calendarViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        // Initialize authentication
        authViewModel.initialize(context)

        // Wait until auth is ready
        snapshotFlow { authViewModel.authState.value }
            .collectLatest { state ->
                if (state.isInitialized && state.isLoggedIn &&
                    uiState.allEvents.isEmpty() && !uiState.isLoading
                ) {
                    // Load events silently after login
                    calendarViewModel.loadAllEvents(context)
                }
            }
    }

    // Combine readiness into a single flag
    val isInitializing = !authState.isInitialized ||
            (authState.isLoggedIn && uiState.isLoading)

    // Show spinner only while still initializing (auth or first event load)
    if (isInitializing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Log.d("AuthCheck", "initialized=${authState.isInitialized}, loggedIn=${authState.isLoggedIn}")

    // Decide start destination based on login state
    val startDestination = if (authState.isLoggedIn) "weeklyCalendar" else "login"

    val currentDestination = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            // only show bottom bar if user is logged in and not on the login page
            if (authState.isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
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
                        forceMode = CalendarMode.DAILY,
                        onEventNavigate = { event ->
                            navigateWithHistory(navController, navHistory, "eventView/${event.id}")
                        }
                    )
                }
                composable("weeklyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.WEEKLY,
                        onEventNavigate = { event ->
                            navigateWithHistory(navController, navHistory, "eventView/${event.id}")
                        }
                    )
                }
                composable("monthlyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.MONTHLY,
                        onEventNavigate = { event ->
                            navigateWithHistory(navController, navHistory, "eventView/${event.id}")
                        }
                    )
                }
                composable("accounts") {
                    AccountsScreen(
                        accountsViewModel = accountsViewModel,
                        navController = navController,
                        authViewModel = authViewModel   // <-- the only change
                    )
                }
                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            navigateWithHistory(navController, navHistory, "weeklyCalendar")
                        }
                    )
                }
                composable(
                    route = "eventView/{eventId}",
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")
                    EventView(eventId, eventViewModel)
                }
            }

            if (authState.isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                FloatingAccountButton(
                    { navigateWithHistory(navController, navHistory, "accounts") },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}
