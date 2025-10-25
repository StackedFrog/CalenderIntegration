package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn


import com.example.calenderintegration.model.GoogleAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val googleSignIn: GoogleSignIn,
    private val accountRepository: GoogleAccountRepository
) {

    private val _currentAccount = MutableStateFlow<GoogleAccount?>(null)
    val currentAccount: StateFlow<GoogleAccount?> = _currentAccount.asStateFlow()
    // Tracks all signed-in Google accounts
    private val _accounts = MutableStateFlow<List<GoogleAccount>>(emptyList())
    val accounts: StateFlow<List<GoogleAccount>> = _accounts.asStateFlow()

    /**
     * Checks whether a user is currently logged in.
     *
     * @return `true` if a Google account is active, `false` otherwise.
     */
    fun isLoggedIn(): Boolean {
        return _currentAccount.value != null
    }

    /**
     * Performs an interactive Google sign-in and saves the authenticated account.
     *
     * Workflow:
     * - Launches Google's sign-in intent using [startIntentSender].
     * - If successful, persists the account via [GoogleAccountRepository].
     * - Updates [_currentAccount] and emits the new state.
     *
     * @param context Android context required for Google sign-in.
     * @param startIntentSender Callback used to trigger the sign-in UI flow.
     * @return The authenticated [GoogleAccount], or `null` if sign-in fails.
     */
    suspend fun logIn(context: Context, startIntentSender: (IntentSenderRequest) -> Unit): GoogleAccount? {
        return try {
            Log.d("GoogleAPI", "Performing full interactive login as requested by user.")

            val newAccount = googleSignIn.performFullGoogleLogin(context, startIntentSender)
            if (newAccount != null) {
                val existing = _accounts.value.toMutableList()
                if (existing.none { it.email == newAccount.email }) {
                    existing.add(newAccount)
                }
                _accounts.value = existing

                accountRepository.saveAccounts(context, existing)
                _currentAccount.value = newAccount
                delay(100)
                Log.d("AuthRepository", "Added account ${newAccount.email}")
            } else {
                Log.e("AuthRepository", "performFullGoogleLogin returned null account")
            }

            newAccount
        } catch (e: Exception) {
            Log.e("GoogleAPI", "Sign-in failed", e)
            null
        }
    }


    /**
     * Signs the current user out and removes the account from local storage.
     *
     * Behavior:
     * - Deletes the active account from [GoogleAccountRepository].
     * - Clears [_currentAccount] to reflect the logout state.
     *
     * @param context Android context used for local account persistence.
     */
    fun logOut(context: Context) {
        _currentAccount.value?.let { account ->
            Log.d("GoogleAPI", "Signing out ${account.email}")

            // Remove this account from persisted list
            val currentAccounts = accountRepository.loadAccounts(context).toMutableList()
            currentAccounts.removeAll { it.email == account.email }
            accountRepository.saveAccounts(context, currentAccounts)
        }

        _currentAccount.value = null
    }

    /**
     * Switches the active account to a different one that is already stored.
     *
     * Behavior:
     * - Loads all stored accounts from [GoogleAccountRepository].
     * - Finds the account matching the given [email].
     * - Updates [_currentAccount] if found, logs a warning if not.
     *
     * @param context Android context used for reading stored accounts.
     * @param email The email address of the account to activate.
     */
    fun switchAccount(context: Context, email: String) {
        val stored = accountRepository.loadAccounts(context)
        val selected = stored.find { it.email == email }

        if (selected != null) {
            _currentAccount.value = selected
            Log.d("AuthRepository", "Switched active account to ${selected.email}")
        } else {
            Log.w("AuthRepository", "Account $email not found in storage")
        }
    }
}
