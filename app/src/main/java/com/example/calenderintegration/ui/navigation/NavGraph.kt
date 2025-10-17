package com.example.calenderintegration.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calenderintegration.ui.calendar.CalendarMode
import com.example.calenderintegration.ui.calendar.CalendarScreen
import com.example.calenderintegration.ui.calendar.CalendarViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination?.route

    // one viewModel for all calendar views
    val calendarViewModel: CalendarViewModel = viewModel()

    Scaffold(
        bottomBar =  {
            NavigationBar (windowInsets = NavigationBarDefaults.windowInsets)
            {
                NavigationBarItem(
                    selected = currentDestination == "dailyCalendar",
                    onClick = { navController.navigate("dailyCalendar") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Daily") }
                )
                NavigationBarItem(
                    selected = currentDestination == "weeklyCalendar",
                    onClick = { navController.navigate("weeklyCalendar") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    } },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    label = { Text("Weekly") }
                )
                NavigationBarItem(
                    selected = currentDestination == "monthlyCalendar",
                    onClick = { navController.navigate("monthlyCalendar") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    } },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Monthly") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "weeklyCalendar") {
            composable("dailyCalendar") { CalendarScreen(calendarViewModel = calendarViewModel, forceMode = CalendarMode.DAILY) }
            composable("weeklyCalendar") { CalendarScreen(calendarViewModel = calendarViewModel, forceMode = CalendarMode.WEEKLY) }
            composable("monthlyCalendar") { CalendarScreen(calendarViewModel = calendarViewModel, forceMode = CalendarMode.MONTHLY) }
        }
    }
}