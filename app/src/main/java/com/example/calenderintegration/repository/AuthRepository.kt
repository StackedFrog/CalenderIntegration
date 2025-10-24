package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn


import com.example.calenderintegration.model.GoogleAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val googleSignIn: GoogleSignIn,
    private val accountRepository: GoogleAccountRepository
)
{

    private val _currentAccount = MutableStateFlow<GoogleAccount?>(null)
    val currentAccount: StateFlow<GoogleAccount?> = _currentAccount.asStateFlow()

    /**
     *
     * Checks if the user is logged in.
     * Returns true if the user is logged in, false otherwise.
     */
    fun isLoggedIn(): Boolean
    {
        return _currentAccount.value != null
    }

    /**
     *
     * Signs the user in.
     * Returns the Google Account that successfully logged in or null if something failed
     */
    suspend fun logIn(context : Context): GoogleAccount?
    {
        return try {
            // ALWAYS perform the interactive login when this function is called.
            // This allows the user to choose an account every time.
            Log.d("GoogleAPI", "Performing full interactive login as requested by user.")
            val account = googleSignIn.performFullGoogleLogin(context)

            if (account != null) {
                // If login is successful, save the account and set it as current
                accountRepository.saveAccounts(context, listOf(account)) // Or add to a list
                _currentAccount.value = account
            }
            return account

        } catch (e: Exception) {
            Log.e("GoogleAPI", "Sign-in failed", e)
            null
        }
    }

    /**
     * Signs the user out by clearing the saved account
     */
    fun logOut(context : Context)
    {
        _currentAccount.value?.let { account ->
            Log.d("GoogleAPI", "Signing out ${account.email}")

            // Remove the account from the repository
            val currentAccounts = accountRepository.loadAccounts(context).toMutableList()
            currentAccounts.removeAll { it.email == account.email }
            accountRepository.saveAccounts(context, currentAccounts)
        }

        _currentAccount.value = null
    }


}