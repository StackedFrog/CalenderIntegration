package com.example.calenderintegration.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.core.net.toUri
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn
import com.example.calenderintegration.api.zohoapi.Config.CLIENT_ID
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import com.example.calenderintegration.api.zohoapi.ZohoAuthManager
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.ZohoAccount
import com.example.calenderintegration.model.ZohoToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val googleSignIn: GoogleSignIn,
    private val googleAccountRepository: GoogleAccountRepository,
    private val zohoAuthManager: ZohoAuthManager,
    private val zohoAccountRepository: ZohoAccountRepository
) {

    // ================= General =================

    /** Loads saved Google accounts for now **/
    fun loadSavedAccounts(context: Context): List<GoogleAccount> {
        return loadSavedGoogleAccounts(context)
    }

    // ================= Google =================

    fun loadSavedGoogleAccounts(context: Context): List<GoogleAccount> {
        val saved = googleAccountRepository.loadAccounts(context)
        Log.d(
            "AuthRepository",
            if (saved.isNotEmpty()) "Restored Google account ${saved.first().email}"
            else "No saved Google accounts found"
        )
        return saved
    }

    suspend fun logInGoogle(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ): GoogleAccount? {
        return try {
            Log.d("AuthRepository", "Starting Google login...")
            val account = googleSignIn.performFullGoogleLogin(context, startIntentSender)
            account?.let {
                val existing = googleAccountRepository.loadAccounts(context).toMutableList()
                if (existing.none { acc -> acc.email == it.email }) {
                    existing.add(it)
                    googleAccountRepository.saveAccounts(context, existing)
                }
                Log.d("AuthRepository", "Added Google account ${it.email}")
            }
            account
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google login failed", e)
            null
        }
    }

    fun logOutGoogle(context: Context, account: GoogleAccount?) {
        if (account == null) return
        val updated = googleAccountRepository.loadAccounts(context).toMutableList()
        updated.removeAll { it.email == account.email }
        googleAccountRepository.saveAccounts(context, updated)
        Log.d("AuthRepository", "Logged out Google account ${account.email}")
    }

    // ================= Zoho =================

    fun getZohoLoginUrl(): Uri {
        val state = System.currentTimeMillis().toString()
        return "https://accounts.zoho.com/oauth/v2/auth".toUri().buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", "com.myzoho://oauth2redirect")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter(
                "scope",
                "ZohoCalendar.calendar.READ,AaaServer.profile.READ"
            )
            .appendQueryParameter("state", state)
            .build()
    }

    suspend fun logInZoho(context: Context, authCode: String): ZohoAccount? {
        return try {
            // ---- TOKEN + EMAIL EXCHANGE WITH TIMEOUT ----
            val (zohoToken, emailValue) = withTimeoutOrNull(30_000L) {
                suspendCancellableCoroutine<Pair<ZohoToken, String>> { cont ->
                    zohoAuthManager.exchangeToken(
                        authCode,
                        onSuccess = { accessToken, email ->
                            val token = ZohoToken(
                                accessToken = accessToken,
                                refreshToken = zohoAuthManager.refreshToken,
                                expiresIn = zohoAuthManager.expiryTime - System.currentTimeMillis()
                            )
                            cont.resume(token to email)
                        },
                        onError = { e -> cont.resumeWithException(e) }
                    )
                    cont.invokeOnCancellation {
                        Log.d("AuthRepository", "Token exchange cancelled")
                    }
                }
            } ?: run {
                Log.e("AuthRepository", "Zoho token exchange timed out")
                return null
            }

            Log.d("AuthRepository", "Zoho token exchange success for $emailValue")

            // ---- CREATE ACCOUNT ----
            val account = ZohoAccount(
                email = emailValue,
                accessToken = zohoToken.accessToken,
                refreshToken = zohoToken.refreshToken,
                expiresIn = zohoToken.expiresIn
            )

            // ---- PERSIST ACCOUNT ----
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
                onSuccess = { accessToken, _ ->
                    val token = ZohoToken(
                        accessToken = accessToken,
                        refreshToken = zohoAuthManager.refreshToken,
                        expiresIn = zohoAuthManager.expiryTime - System.currentTimeMillis()
                    )
                    cont.resume(token)
                },
                onError = { e -> cont.resumeWithException(e) }
            )
        }
}
