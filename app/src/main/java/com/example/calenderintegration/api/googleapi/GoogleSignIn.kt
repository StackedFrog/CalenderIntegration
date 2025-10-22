package com.example.calenderintegration.api.googleapi



import android.accounts.Account
import android.content.Context
import android.util.Log
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
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

import androidx.activity.result.IntentSenderRequest


object GoogleSignIn
{

    // this sign in function that just signs you in without getting access to the calendar data
    suspend fun signIn(context: Context): String? {
        val credentialManager = CredentialManager.create(context)
        delay(250)

        return try {
            val googleOption = GetSignInWithGoogleOption.Builder(CalendarConstants.WEB_CLIENT_ID)
                .setNonce(generateSecureRandomNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .setPreferImmediatelyAvailableCredentials(false)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                Log.i("GoogleSignIn", "Signed in as ${googleCred.id}")
                googleCred.id // email address
            } else {
                Log.e("GoogleSignIn", "Unexpected credential type: ${credential.type}")
                null
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Sign-in failed: ${e.message}", e)
            null
        }
    }






    //this is the one that will get the access to calendar data
    suspend fun authorizeCalendarAccess(
        context: Context,
        email: String,
        startIntentSender: ((IntentSenderRequest) -> Unit)? = null
    ): String? {
        val account = Account(email, "com.google")
        val authClient = Identity.getAuthorizationClient(context)

        return suspendCoroutine { continuation ->
            val request = AuthorizationRequest.Builder()
                .setRequestedScopes(CalendarConstants.SCOPES)
                .setAccount(account)
                .requestOfflineAccess(CalendarConstants.WEB_CLIENT_ID)
                .build()

            authClient.authorize(request)
                .addOnSuccessListener { result ->
                    when {
                        // Case 1: token immediately available
                        result.accessToken != null -> {
                            Log.d("GoogleAuth", "Access token retrieved for $email: ${result.accessToken}")
                            continuation.resume(result.accessToken)
                        }

                        // Case 2: requires user interaction
                        result.hasResolution() -> {
                            Log.d("GoogleAuth", "Launching authorization intent for $email")
                            startIntentSender?.invoke(
                                IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build()
                            )
                            continuation.resume(null)
                        }

                        // Case 3: no token and no resolution
                        else -> {
                            Log.e("GoogleAuth", "No access token and no resolution for $email")
                            continuation.resume(null)
                        }
                    }
                }
                .addOnFailureListener { err ->
                    Log.e("GoogleAuth", "Authorization failed for $email: ${err.message}", err)
                    continuation.resume(null)
                }
        }
    }




// This will use the both function from above to get the calendar data from one account, also saves the account data for future use
    suspend fun performFullGoogleLogin(
        context: Context,
        startIntentSender: (IntentSenderRequest) -> Unit
    ): GoogleAccount? {
        val email = signIn(context) ?: return null
        delay(250)

        Log.d("GoogleAuth", "Starting fresh authorization for $email")

        val accessToken = authorizeCalendarAccess(
            context = context,
            email = email,
            startIntentSender = startIntentSender
        )

        Log.d("GoogleAuth", "Access token for $email = $accessToken")

        val account = GoogleAccount(
            email = email,
            displayName = email.substringBefore("@"),
            accessToken = accessToken
        )

        saveAccount(account, context)//saves account
        Log.d("GoogleAuth", "Account saved: $email")

        return account
    }



// tokens expire after one hour, they have to refreshed, but not to worry, this should be handled by this function being called by fetch, create, delete functions
    suspend fun refreshAccessToken(context: Context, account: GoogleAccount): GoogleAccount? {
        val newToken = authorizeCalendarAccess(context, account.email)
        if (newToken != null) {
            val updatedAccount = account.copy(accessToken = newToken)
            saveAccount(updatedAccount, context)
            Log.d("GoogleAuth", "Refreshed token for ${account.email}")
            return updatedAccount
        }
        Log.e("GoogleAuth", "Failed to refresh token for ${account.email}")
        return null
    }



    //function that will save the account once logged in,
    fun saveAccount(account: GoogleAccount, context: Context) {
        GoogleAccountRepository.addAccount(context, account)
    }


    //security thing for google sign in to work
    fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}




