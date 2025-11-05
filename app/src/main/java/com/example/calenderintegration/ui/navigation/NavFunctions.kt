package com.example.calenderintegration.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController

/**
 * Navigate while keeping a simple in-memory history of destinations.
 * - Skips navigating if you're already on the same route.
 * - Records the destination (no consecutive duplicates).
 * - Optionally caps history length to avoid unbounded growth.
 */
fun navigateWithHistory(
    navController: NavHostController,
    navHistory: SnapshotStateList<String>,
    destination: String
) {
    val currentRoute = navController.currentDestination?.route
    if (currentRoute == destination) return

    // Record history (avoid consecutive duplicates)
    if (navHistory.lastOrNull() != destination) {
        navHistory.add(destination)
        // Optional: cap the history size
        if (navHistory.size > 50) {
            navHistory.removeAt(0)
        }
    }

    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Convenience wrapper for navigating to a specific Event screen.
 */
fun navigateToEvent(
    navController: NavHostController,
    navHistory: SnapshotStateList<String>,
    eventId: String
) {
    navigateWithHistory(navController, navHistory, "event/$eventId")
}
