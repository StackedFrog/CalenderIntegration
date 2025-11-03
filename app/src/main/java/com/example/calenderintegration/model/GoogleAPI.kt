package com.example.calenderintegration.model

import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.CalendarApiService
import com.example.calenderintegration.api.googleapi.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine


object GoogleAPI {

    // --- References to objects needed for API usage ---
    private val _google_calendar_service = CalendarApiService
    private val _google_account_repository = GoogleAccountRepository
    private val _google_sign_in = GoogleSignIn

    // --- State flows for UI to observe ---
    private val _currentAccount = MutableStateFlow<GoogleAccount?>(null)
    val currentAccount: StateFlow<GoogleAccount?> = _currentAccount.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val uiScope = CoroutineScope(Dispatchers.Main)





    /**
     * Initiates the Google Sign-In and Authorization flow.
     * Adds the signed-in account to the user's list of accounts.
     */

    suspend fun signIn(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ): GoogleAccount? {

        return try {
            // Try to load a saved account
            val savedAccount = _google_account_repository.loadAccounts(context).firstOrNull()

            if (savedAccount != null) {
                Log.d("GoogleAPI", "Found saved account: ${savedAccount.email}, trying to refresh token...")
                val refreshed = _google_sign_in.refreshAccessToken(context, savedAccount)

                if (refreshed != null) {
                    Log.d("GoogleAPI", "Token refreshed successfully for ${refreshed.email}")
                    _currentAccount.value = refreshed
                    return refreshed
                } else {
                    Log.d("GoogleAPI", "Saved token expired, performing full interactive login for ${savedAccount.email}")
                    val account = _google_sign_in.performFullGoogleLogin(context, startIntentSender)
                    if (account != null) _currentAccount.value = account
                    return account
                }
            } else {
                Log.d("GoogleAPI", "No saved account, performing full interactive login")
                val account = _google_sign_in.performFullGoogleLogin(context, startIntentSender)
                if (account != null) _currentAccount.value = account
                return account
            }
        } catch (e: Exception) {
            Log.e("GoogleAPI", "Sign-in failed", e)
            null
        }
    }


    /**
     * Fetches upcoming calendar events for the signed-in user.
     */
    fun fetchEvents(context: Context) {
        val account = _currentAccount.value
        if (account == null) {
            Log.e("GoogleAPI", "Cannot fetch events: No signed-in account")
            _events.value = emptyList()
            return
        }

        _loading.value = true

        _google_calendar_service.fetchCalendarData(context, account) { fetchedEvents ->
            uiScope.launch {
                _events.value = fetchedEvents
                _loading.value = false
                Log.d("GoogleAPI", "Fetched ${fetchedEvents.size} events for ${account.email}")
            }
        }
    }

    /**
     * Creates a calendar event for the signed-in user.
     */
    fun createEvent(context: Context, event: Event, onResult: (Boolean) -> Unit) {
        val account = _currentAccount.value
        if (account == null) {
            Log.e("GoogleAPI", "Cannot create event: No signed-in account")
            onResult(false)
            return
        }

        _google_calendar_service.createCalendarEvent(context, account, event, onResult)
    }

    /**
     * Deletes a calendar event for the signed-in user.
     */
    fun deleteEvent(context: Context, eventId: String, onResult: (Boolean) -> Unit) {
        val account = _currentAccount.value
        if (account == null) {
            Log.e("GoogleAPI", "Cannot delete event: No signed-in account")
            onResult(false)
            return
        }

        _google_calendar_service.deleteCalendarEvent(context, account, eventId, onResult)
    }


    /**
     * Signs the user out by clearing the saved account and resetting state.
     */
    fun signOut(context: Context) {
        _currentAccount.value?.let { account ->
            Log.d("GoogleAPI", "Signing out ${account.email}")

            // Remove the account from the repository
            val currentAccounts = _google_account_repository.loadAccounts(context).toMutableList()
            currentAccounts.removeAll { it.email == account.email }
            _google_account_repository.saveAccounts(context, currentAccounts)
        }

        // Optionally: remove account from repository
        // For now, just reset state
        _currentAccount.value = null
        _events.value = emptyList()
    }

    /**
     * Loads previously saved accounts and optionally sets the first one as current.
     */
    fun loadSavedAccounts(context: Context) {
        val accounts = _google_account_repository.loadAccounts(context)
        if (accounts.isNotEmpty()) {
            _currentAccount.value = accounts.first()
        }
    }

}