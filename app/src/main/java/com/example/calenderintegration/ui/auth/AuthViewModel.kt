package com.example.calenderintegration.ui.auth

import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calenderintegration.repository.AuthRepository
import com.example.calenderintegration.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState
    suspend fun logIn(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ) {
        try {
            Log.d("AuthViewModel", "Attempting to log in")
            val response = authRepository.logIn(
                context = context,
                startIntentSender = startIntentSender
            )
            if (response != null) {
                _authState.update { it.copy(isLoggedIn = true)
                }
                authRepository.currentAccount = MutableStateFlow(response)

                Log.d("AuthViewModel", "Logged in successfully")
            } else {
                _authState.update { it.copy(isLoggedIn = false) }
            }
        } catch (e: Exception) {
            _authState.update { it.copy(error = e.message) }
        }
    }


    suspend fun logOut(context: Context) {
        try {
            Log.d("AuthViewModel", "Attempting to log out")
            authRepository.logOut(context) // Call the new repository function
        } finally {
            // Always update the local state to reflect logged-out status,
            // even if the credential clearing fails for some reason.
            _authState.update { it.copy(isLoggedIn = false, error = null) }
            Log.d("AuthViewModel", "Logged out")
        }
    }



    fun isLoggedIn(): Boolean {
        return authState.value.isLoggedIn
    }

    /**
     * Fetches and logs events for the current day, week, and month.
     * For testing purposes only
     */
    fun fetchAndLogEvents(context : Context) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentAccountName = authRepository.currentAccount.value?.email ?: "Unknown"


            Log.d("AuthViewModel", "Fetching events for $currentAccountName")



            // Fetch events using the repository functions
            val dailyEvents = calendarRepository.getEventsByDay(context, today)
            val weeklyEvents = calendarRepository.getEventsByWeek(context, today)
            val monthlyEvents = calendarRepository.getEventsByMonth(context, today)

            // Log the results to Logcat for debugging
            Log.d("AuthViewModel", "--- Daily Events for $today ---")
            dailyEvents.forEach { Log.d("AuthViewModel", it.toString()) }

            Log.d("AuthViewModel", "--- Weekly Events ---")
            weeklyEvents.forEach { Log.d("AuthViewModel", it.toString()) }

            Log.d("AuthViewModel", "--- Monthly Events ---")
            monthlyEvents.forEach { Log.d("AuthViewModel", it.toString()) }
        }
    }
}

data class AuthState (
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)