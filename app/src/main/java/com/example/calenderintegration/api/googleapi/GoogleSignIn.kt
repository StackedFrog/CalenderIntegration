package com.example.calenderintegration.api.googleapi

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.calenderintegration.model.GoogleAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.delay
import java.security.SecureRandom
import java.util.Base64
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity

object GoogleSignIn {

    suspend fun signIn(context: Context): GoogleAccount? {
        val credentialManager = CredentialManager.create(context)
        delay(250)

        return try {
            // Step 1: Google ID sign-in (identity)
            val result = credentialManager.getCredential(
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetSignInWithGoogleOption.Builder(CalendarConstants.WEB_CLIENT_ID)
                            .setNonce(generateSecureRandomNonce())
                            .build()
                    )
                    .build(),
                context = context
            )

            val credential = result.credential
            val googleIdCredential = credential as? CustomCredential
            val idToken = googleIdCredential?.data?.getString("idToken")
            val email = googleIdCredential?.data?.getString("email") ?: ""
            val name = googleIdCredential?.data?.getString("displayName") ?: ""

            // Step 2: Request access
            var accessToken: String? = null

            val latch = java.util.concurrent.CountDownLatch(1)

            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(CalendarConstants.SCOPES)
                .requestOfflineAccess(CalendarConstants.WEB_CLIENT_ID)
                .build()

            Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .addOnSuccessListener { authResult ->
                    if (authResult.hasResolution()) {
                        // If user consent is required, handle with IntentSenderRequest
                        Log.w("GoogleSignIn", "Authorization requires user resolution.")
                    } else {
                        accessToken = authResult.accessToken
                    }
                    latch.countDown()
                }
                .addOnFailureListener {
                    Log.e("GoogleSignIn", "Authorization failed: ${it.message}", it)
                    latch.countDown()
                }

            latch.await() // Wait for access token result before proceeding

            // Step 3: Create GoogleAccount and save it
            if (idToken != null && accessToken != null) {
                val account = GoogleAccount(
                    email = email,
                    displayName = name,
                    idToken = idToken,
                    accessToken = accessToken!!

                )
                saveAccount(account, context)
                Log.d("GoogleAccountRepo", "Signed in as ${account.email}")
                account
            } else {
                Log.e("GoogleAccountRepo", "Missing token(s): idToken=$idToken, accessToken=$accessToken")
                Toast.makeText(context, "Sign-in failed: Missing tokens", Toast.LENGTH_SHORT).show()
                null
            }

        } catch (e: Exception) {
            Log.e("GoogleAccountRepo", "Sign-in failed", e)
            null
        }
    }


    private fun saveAccount(account: GoogleAccount, context: Context) {
        GoogleAccountRepository.addAccount(context, account)
    }

    fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}