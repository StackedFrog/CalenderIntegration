package com.example.calenderintegration.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.calenderintegration.MainActivity
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import com.example.calenderintegration.api.zohoapi.ZohoAuthManager
import com.example.calenderintegration.model.ZohoAccount
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthRedirectActivity : ComponentActivity() {

    private val TAG = "ZohoRedirect"

    private val authViewModel: AuthViewModel by viewModels()

    // Inject Zoho classes
    @Inject lateinit var zohoAuthManager: ZohoAuthManager
    @Inject lateinit var zohoAccountRepository: ZohoAccountRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri?.toString()?.startsWith("com.myzoho://oauth2redirect") == true) {
            val code = uri.getQueryParameter("code")

            if (code != null) {
                Log.d(TAG, "✅ Auth code received: $code")

                lifecycleScope.launch {
                    try {
                        // ────────────────────────────────────────────────
                        // 1️⃣ Exchange the auth code for tokens and email
                        // ────────────────────────────────────────────────
                        zohoAuthManager.exchangeToken(
                            authCode = code,
                            onSuccess = { accessToken, email ->

                                Log.d(TAG, "✅ Access token obtained for $email")

                                // ────────────────────────────────────────────────
                                // 2️⃣ Create a ZohoAccount object
                                // ────────────────────────────────────────────────
                                val account = ZohoAccount(
                                    email = email,
                                    accessToken = accessToken,
                                    refreshToken = zohoAuthManager.refreshToken,
                                    expiresIn = zohoAuthManager.expiryTime
                                )

                                // ────────────────────────────────────────────────
                                // 3️⃣ Save the Zoho account to device storage
                                // ────────────────────────────────────────────────
                                zohoAccountRepository.addAccount(
                                    context = this@AuthRedirectActivity,
                                    account = account
                                )
                                Log.d(TAG, "💾 Zoho account saved locally for: ${account.email}")

                                // ────────────────────────────────────────────────
                                // 4️⃣ Redirect to MainActivity (weekly calendar)
                                // ────────────────────────────────────────────────
                                val intent = Intent(
                                    this@AuthRedirectActivity,
                                    MainActivity::class.java
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    putExtra("navigateTo", "weeklyCalendar")
                                }
                                startActivity(intent)
                                finish()
                            },
                            onError = { error ->
                                Log.e(TAG, "❌ Zoho login failed", error)
                                finish()
                            }
                        )

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error during Zoho login", e)
                        finish()
                    }
                }

            } else {
                Log.e(TAG, "⚠️ No auth code found in redirect URI.")
                finish()
            }
        } else {
            finish()
        }
    }
}