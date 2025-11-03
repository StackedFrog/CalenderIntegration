package com.example.calenderintegration.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthRedirectActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val TAG = "ZohoRedirect"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri?.toString()?.startsWith("com.myzoho://oauth2redirect") == true) {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                Log.d(TAG, "Auth code received: $code")

                lifecycleScope.launch {
                    try {
                        // Start login
                        authViewModel.logInZoho(this@AuthRedirectActivity, code)

                        // Wait for the repository call to actually finish
                        authViewModel.authState
                            .first { state ->
                                !state.isLoading && (state.isLoggedIn || state.error != null)
                            }

                        Log.d(TAG, "Zoho login fully completed. Closing.")
                        finish()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in login flow", e)
                        finish()
                    }
                }
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}