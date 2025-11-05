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
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.flow.collectLatest

private const val EVENT_ROUTE = "event/{eventId}"

@Composable
fun MainScreen(context: Context = LocalContext.current) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val navHistory = remember { mutableStateListOf<String>() }

    // viewModels for screens
    val calendarViewModel: CalendarViewModel = viewModel()
    val accountsViewModel: AccountsViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // Collect UI states
    val uiState by calendarViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        // Initialize authentication
        authViewModel.initialize(context)

        // After auth initializes/logs in, load events once if empty
        snapshotFlow { authViewModel.authState.value }
            .collectLatest { state ->
                if (state.isInitialized && state.isLoggedIn &&
                    uiState.allEvents.isEmpty() && !uiState.isLoading
                ) {
                    calendarViewModel.loadAllEvents(context)
                }
            }
    }

    val isInitializing = !authState.isInitialized ||
            (authState.isLoggedIn && uiState.isLoading)

    if (isInitializing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    Log.d("AuthCheck", "initialized=${authState.isInitialized}, loggedIn=${authState.isLoggedIn}")

    val startDestination = if (authState.isLoggedIn) "weeklyCalendar" else "login"
    val currentDestination = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            if (authState.isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    NavigationBarItem(
                        selected = currentDestination == "dailyCalendar",
                        onClick = { navigateWithHistory(navController, navHistory, "dailyCalendar") },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Daily") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "weeklyCalendar",
                        onClick = { navigateWithHistory(navController, navHistory, "weeklyCalendar") },
                        icon = { Icon(Icons.Default.Delete, null) },
                        label = { Text("Weekly") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == "monthlyCalendar",
                        onClick = { navigateWithHistory(navController, navHistory, "monthlyCalendar") },
                        icon = { Icon(Icons.Default.Build, null) },
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
                            navigateToEvent(navController, navHistory, event.id)
                        }
                    )
                }
                composable("weeklyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.WEEKLY,
                        onEventNavigate = { event ->
                            navigateToEvent(navController, navHistory, event.id)
                        }
                    )
                }
                composable("monthlyCalendar") {
                    CalendarScreen(
                        calendarViewModel = calendarViewModel,
                        forceMode = CalendarMode.MONTHLY,
                        onEventNavigate = { event ->
                            navigateToEvent(navController, navHistory, event.id)
                        }
                    )
                }
                composable("accounts") {
                    AccountsScreen(
                        accountsViewModel = accountsViewModel,
                        navController = navController,
                        authViewModel = authViewModel
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
                    route = EVENT_ROUTE,
                    arguments = listOf(navArgument("eventId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId")
                    EventView(eventId = eventId)
                }
            }

            if (authState.isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                FloatingAccountButton(
                    { navigateWithHistory(navController, navHistory, "accounts") },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // TEMP FAB: open EventView in CREATE mode (works even if there are no events yet)
            if (authState.isLoggedIn && currentDestination != "login" && currentDestination != "accounts") {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        navigateWithHistory(navController, navHistory, "event/new")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Create")
                }
            }
        }
    }
}
