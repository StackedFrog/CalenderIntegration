package com.example.calenderintegration.ui.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calenderintegration.ui.calendar.CalendarMode
import com.example.calenderintegration.ui.calendar.CalendarScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(bottomBar = BottomNavBar(navController = navController)) {

    }
    NavHost(navController = navController, startDestination = "weeklyCalender") {
        composable("dailyCalendar") { CalendarScreen(forceMode = CalendarMode.DAILY) }
        composable("weeklyCalendar") { CalendarScreen(forceMode = CalendarMode.WEEKLY) }
        composable("monthlyCalendar") { CalendarScreen(forceMode = CalendarMode.MONTHLY) }
    }
}

@Composable
fun BottomNavBar(navController) {
    Button(onClick = { navController.navigate(somewhere)})

}