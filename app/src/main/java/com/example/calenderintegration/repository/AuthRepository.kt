package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.ClearCredentialStateRequest
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn
import androidx.credentials.CredentialManager

import com.example.calenderintegration.model.GoogleAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val googleSignIn: GoogleSignIn,
    private val accountRepository: GoogleAccountRepository)
{

    private var _currentAccount = MutableStateFlow<GoogleAccount?>(null)
    var currentAccount: StateFlow<GoogleAccount?> = _currentAccount.asStateFlow()


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
    suspend fun logIn(context : Context, startIntentSender: (IntentSenderRequest) -> Unit): GoogleAccount?
    {
        return try {
            // ALWAYS perform the interactive login when this function is called.
            // This allows the user to choose an account every time.
            Log.d("GoogleAPI", "Performing full interactive login as requested by user.")

            val account = googleSignIn.performFullGoogleLogin(context, startIntentSender)

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
     * Not fucking working
     */
    suspend fun logOut(context: Context) {
        try {
            /**
            // 1. Revoke the token with Google.
            // This invalidates the token on Google's servers.
            googleSignIn.revokeToken()
            Log.d("AuthRepository", "Google token revoked successfully.")

            // 2. Sign out from the GoogleSignInClient.
            // This clears the local sign-in state for the Google Play Services client.
            googleSignIn.signOut()
            Log.d("AuthRepository", "GoogleSignInClient signed out successfully.")

            **/
            // 3. Clear the credential state using CredentialManager.
            // This handles the newer credential management system.
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d("AuthRepository", "Credential state cleared successfully.")

            // 4. Clear the local account state in your app.
            _currentAccount.value = null

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during logout", e)
            // Even if one step fails, try to clear the local state as a fallback.
            _currentAccount.value = null
        }
    }

}


