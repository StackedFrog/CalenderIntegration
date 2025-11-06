package com.example.calenderintegration

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import com.example.calenderintegration.api.zohoapi.ZohoAuthManager
import com.example.calenderintegration.model.ZohoAccount
import com.example.calenderintegration.ui.navigation.MainScreen
import com.example.calenderintegration.ui.theme.CalenderIntegrationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var zohoAuthManager: ZohoAuthManager
    @Inject lateinit var zohoAccountRepo: ZohoAccountRepository

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle Zoho OAuth deep link: com.myzoho://oauth2redirect?code=...
        intent?.data?.let { uri ->
            if (uri.scheme == "com.myzoho" && uri.host == "oauth2redirect") {
                val code = uri.getQueryParameter("code")
                if (!code.isNullOrEmpty()) {
                    zohoAuthManager.exchangeToken(
                        authCode = code,
                        onSuccess = { accessToken, email ->
                            val acc = ZohoAccount(
                                email = email,
                                accessToken = accessToken,
                                refreshToken = zohoAuthManager.refreshToken,
                                expiresIn = (zohoAuthManager.expiryTime - System.currentTimeMillis()) / 1000
                            )
                            zohoAccountRepo.addAccount(this, acc)
                            runOnUiThread {
                                Toast.makeText(this, "Zoho linked: $email", Toast.LENGTH_SHORT).show()
                                // Navigate to Accounts after return to Compose
                                intent.removeExtra("navigateTo")
                                intent.putExtra("navigateTo", "accounts")
                                render()
                            }
                        },
                        onError = { e ->
                            runOnUiThread {
                                Toast.makeText(this, "Zoho auth failed: ${e.message}", Toast.LENGTH_LONG).show()
                                render()
                            }
                        }
                    )
                    return // wait for async callback; render() will be called there
                }
            }
        }

        // Normal render (no Zoho callback)
        render()
    }

    private fun render() {
        enableEdgeToEdge()
        setContent {
            CalenderIntegrationTheme {
                MainScreen()
            }
        }
    }
}
