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
    /** Loads all saved Google accounts and returns them. */
    fun loadSavedAccounts(context: Context): List<GoogleAccount> {
        val saved = accountRepository.loadAccounts(context)
        Log.d("AuthRepository", if (saved.isNotEmpty())
            "Restored account ${saved.first().email}"
        else
            "No saved accounts found")
        return saved
    }

    /** Performs interactive Google login and returns the signed-in account if successful. */
    suspend fun logIn(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ): GoogleAccount? {
        return try {
            Log.d("GoogleAPI", "Performing full interactive login as requested by user.")
            val account = googleSignIn.performFullGoogleLogin(context, startIntentSender)
            if (account != null) {
                val existing = accountRepository.loadAccounts(context).toMutableList()
                if (existing.none { it.email == account.email }) {
                    existing.add(account)
                    accountRepository.saveAccounts(context, existing)
                }
                Log.d("AuthRepository", "Added account ${account.email}")
            }
            account
        } catch (e: Exception) {
            Log.e("GoogleAPI", "Sign-in failed", e)
            null
        }
    }

    /** Deletes the current account from local storage. */
    fun logOut(context: Context, account: GoogleAccount?) {
        if (account == null) return
        val updated = accountRepository.loadAccounts(context).toMutableList()
        updated.removeAll { it.email == account.email }
        accountRepository.saveAccounts(context, updated)
        Log.d("AuthRepository", "Logged out ${account.email}")
    }
}

