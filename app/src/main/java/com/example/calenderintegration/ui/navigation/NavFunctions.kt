package com.example.calenderintegration.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController


fun navigateWithHistory(navController: NavHostController, navHistory: SnapshotStateList<String>, destination: String) {
    // Add destination to history
    val currentRoute = navController.currentDestination?.route

    // Avoid re-navigating to the same destination
    if (currentRoute == destination) return

    navController.navigate(destination) {
        // Pop up to start destination to keep back stack small and clean
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}