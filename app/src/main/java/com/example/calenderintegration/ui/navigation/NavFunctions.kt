package com.example.calenderintegration.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController


fun navigateWithHistory(navController: NavHostController, navHistory: SnapshotStateList<String>, destination: String) {
    // Add destination to history
    if (navHistory.contains(destination)) {
        // Remove if already exists to avoid duplicates
        navHistory.remove(destination)
    }
    navHistory.add(destination)

    // Keep only the last 5 destinations
    if (navHistory.size > 5) {
        navHistory.removeAt(0)
    }

    // Navigate
    navController.navigate(destination) {
        // Pop up to the first in history, if any
        if (navHistory.size > 1) {
            popUpTo(navHistory.first()) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}
