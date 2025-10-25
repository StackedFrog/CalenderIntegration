package com.example.calenderintegration

import android.R.attr.delay
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.calenderintegration.ui.auth.AuthViewModel
import com.example.calenderintegration.ui.auth.LoginScreen
import com.example.calenderintegration.ui.calendar.CalendarViewModel
import com.example.calenderintegration.ui.calendar.DailyView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val calendarViewModel: CalendarViewModel = hiltViewModel()
                val authState by authViewModel.authState.collectAsState()
                val uiState by calendarViewModel.uiState.collectAsState()
                val context = LocalContext.current

                if (authState.isLoggedIn) {
                    Log.d("MainActivity", "User logged in → loading all events")
                    LaunchedEffect(Unit) {
                        calendarViewModel.loadAllEvents(context)
                    }

                    DailyView(
                        calendarViewModel = calendarViewModel,
                        uiState = uiState,
                        onEventClick = { event ->
                            Log.d("DailyView", "Clicked event: ${event.summary}")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            Log.d("MainActivity", "Login success → fetching events next")
                            calendarViewModel.loadAllEvents(context)
                        }
                    )
                }

            }
        }
    }
}

