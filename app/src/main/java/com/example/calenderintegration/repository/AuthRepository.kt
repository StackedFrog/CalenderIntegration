package com.example.calenderintegration.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn
import com.example.calenderintegration.api.zohoapi.Config.CLIENT_ID
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
import androidx.core.net.toUri
import kotlinx.coroutines.withTimeoutOrNull


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

    // ================= General ============

    /** Supposed to return both google and zoho accounts **/
    fun loadSavedAccounts(context : Context): List<GoogleAccount>{
        /**Loads only google accounts as of now **/
        return loadSavedGoogleAccounts(context)
    }

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

    // In AuthRepository
    fun getZohoLoginUrl(): Uri {
        val state = System.currentTimeMillis().toString()
        // Save state if needed for validation
        return "https://accounts.zoho.com/oauth/v2/auth".toUri().buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", "com.myzoho://oauth2redirect")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "ZohoCalendar.calendar.READ")
            .appendQueryParameter("state", state)
            .build()
    }

    /**
     *
     * Pablo 1/11: Function for logging in with Zoho but I'm not sure about it
     */
    suspend fun logInZoho(context: Context, authCode: String): ZohoAccount? {
        return try {
            // ---- TOKEN EXCHANGE WITH 30-second TIMEOUT ----
            val token = withTimeoutOrNull(30_000L) {
                suspendCancellableCoroutine<ZohoToken> { cont ->
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
                    cont.invokeOnCancellation {
                        Log.d("AuthRepository", "Token exchange cancelled")
                    }
                }
            } ?: run {
                Log.e("AuthRepository", "Token exchange timed out")
                return null
            }

            Log.d("AuthRepository", "Token exchange SUCCESS: ${token.accessToken}")

            // ---- CREATE ACCOUNT (use placeholder email for now) ----
            val account = ZohoAccount(
                email = "temp@zoho.com", // TODO: fetch real email later
                accessToken = token.accessToken,
                refreshToken = token.refreshToken,
                expiresIn = token.expiresIn
            )

            // ---- PERSIST ----
            val existing = zohoAccountRepository.loadAccounts(context).toMutableList()
            if (existing.none { it.email == account.email }) {
                existing.add(account)
                zohoAccountRepository.saveAccounts(context, existing)
            }

            Log.d("AuthRepository", "Zoho login SUCCESS: ${account.email}")
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