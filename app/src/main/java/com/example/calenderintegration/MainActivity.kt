package com.example.calenderintegration

import android.os.Bundle

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.example.calenderintegration.ui.auth.AuthViewModel
import com.example.calenderintegration.ui.auth.LoginScreen
import com.example.calenderintegration.ui.calendar.CalendarUiState
import com.example.calenderintegration.ui.calendar.CalendarViewModel
import com.example.calenderintegration.ui.calendar.DailyView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Get the real Hilt-injected ViewModel
                val calendarViewModel: CalendarViewModel = hiltViewModel()
                val uiState by calendarViewModel.uiState.collectAsState()

                // Call once to populate events (optional for now)
                LaunchedEffect(Unit) {
                    calendarViewModel.getAllEventsThisWeek()
                }

                DailyView(
                    calendarViewModel = calendarViewModel,
                    uiState = uiState,
                    onEventClick = { event ->
                        Log.d("DailyView", "Clicked event: ${event.summary}")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

