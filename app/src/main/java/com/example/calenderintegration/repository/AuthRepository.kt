package com.example.calenderintegration.repository

import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import com.example.calenderintegration.api.zohoapi.ZohoAuthManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.ZohoAccount
import com.example.calenderintegration.model.ZohoToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


/**
 *
 * Pablo 1/11: Added Zoho references to AuthRepository AND a few name changes
 *             so distinction can be made between Google and Zoho
 *             example : logIn() ------->  logInGoogle()
 *             Added as well the same Zoho functionalities as we had previously for Google
 *
 */
@Singleton
class AuthRepository @Inject constructor(
    private val googleSignIn: GoogleSignIn,
    private val accountRepository: GoogleAccountRepository,
    private val zohoAuthManager: ZohoAuthManager,
    private val zohoAccountRepository: ZohoAccountRepository
) {

    // ================= Google =================
    /** Loads all saved Google accounts and returns them. */
    fun loadSavedGoogleAccounts(context: Context): List<GoogleAccount> {
        val saved = accountRepository.loadAccounts(context)
        Log.d("AuthRepository", if (saved.isNotEmpty())
            "Restored account ${saved.first().email}"
        else
            "No saved accounts found")
        return saved
    }

    /** Performs interactive Google login and returns the signed-in account if successful. */
    suspend fun logInGoogle(
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
    fun logOutGoogle(context: Context, account: GoogleAccount?) {
        if (account == null) return
        val updated = accountRepository.loadAccounts(context).toMutableList()
        updated.removeAll { it.email == account.email }
        accountRepository.saveAccounts(context, updated)
        Log.d("AuthRepository", "Logged out ${account.email}")
    }

    // ================= Zoho =================

    fun getZohoLoginUrl(): String = zohoAuthManager.getAuthUrl()

    /**
     *
     * Pablo 1/11: Function for logging in with Zoho but I'm not sure about it
     */
    suspend fun logInZoho(context: Context, authCode: String): ZohoAccount? {
        return try {
            // Step 1: Exchange auth code for tokens
            val token = suspendCancellableCoroutine<ZohoToken> { cont ->
                zohoAuthManager.exchangeToken(
                    authCode,
                    onSuccess = {
                        val t = ZohoToken(
                            accessToken = zohoAuthManager.accessToken ?: "",
                            refreshToken = zohoAuthManager.refreshToken,
                            expiresIn = zohoAuthManager.expiryTime - System.currentTimeMillis()
                        )
                        cont.resume(t)
                    },
                    onError = { e -> cont.resumeWithException(e) }
                )
            }

            /**
             * Pablo 1/11: Need API function 'fetchZohoEmail(access_token) to retrieve
             *             the email given the access token so we can create the account
             *             and store it locally
             */
            // Step 2: Fetch the real email (placeholder method)
            //val email = fetchZohoEmail(token.accessToken) //

            // Step 3: Create account object
            val account = ZohoAccount(
                email = "idontknow@zoho.com",   // this is a placeholder since i dont know the email yet
                accessToken = token.accessToken,
                refreshToken = token.refreshToken,
                expiresIn = token.expiresIn
            )

            // Step 4: Persist in repository
            val existing = zohoAccountRepository.loadAccounts(context).toMutableList()
            if (existing.none { it.email == account.email }) {
                existing.add(account)
                zohoAccountRepository.saveAccounts(context, existing)
            }

            Log.d("AuthRepository", "Zoho login successful for ${account.email}")
            account
        } catch (e: Exception) {
            Log.e("AuthRepository", "Zoho login failed", e)
            null
        }
    }

    fun logOutZoho(context: Context, account: ZohoAccount?) {
        if (account == null) return
        val updated = zohoAccountRepository.loadAccounts(context).toMutableList()
        updated.removeAll { it.email == account.email }
        zohoAccountRepository.saveAccounts(context, updated)
        Log.d("AuthRepository", "Logged out Zoho account ${account.email}")
    }

    // ---------------- Helper ----------------
    private suspend fun exchangeZohoToken(authCode: String): ZohoToken =
        suspendCancellableCoroutine { cont ->
            zohoAuthManager.exchangeToken(
                authCode,
                onSuccess = { accessToken ->
                    // accessToken is a String from ZohoAuthManager
                    // But ZohoAuthManager actually also sets refreshToken and expiryTime
                    val token = ZohoToken(
                        accessToken = zohoAuthManager.accessToken ?: "",
                        refreshToken = zohoAuthManager.refreshToken,
                        expiresIn = zohoAuthManager.expiryTime - System.currentTimeMillis()
                    )
                    cont.resume(token)
                },
                onError = { e ->
                    cont.resumeWithException(e)
                }
            )
        }
}

