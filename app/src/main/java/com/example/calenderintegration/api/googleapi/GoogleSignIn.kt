package com.example.calenderintegration.api.googleapi


import android.content.Context
import android.util.Log
import android.widget.Toast

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException

import com.example.calenderintegration.model.GoogleAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.delay
import java.security.SecureRandom
import java.util.Base64
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object GoogleSignIn {

    suspend fun signIn(context: Context): String? { // I was too much of a genius and it served me bad, so I have this sign in function that just signs you in without getting access to the calendar data
        val credentialManager = CredentialManager.create(context)
        delay(250)

        return try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetSignInWithGoogleOption.Builder(CalendarConstants.WEB_CLIENT_ID)
                        .setNonce(generateSecureRandomNonce())
                        .build()
                )
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    Toast.makeText(context, "Signed in as $email", Toast.LENGTH_SHORT).show()
                    Log.i("GoogleSignIn", "Sign-in successful: $email")
                    idToken
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("GoogleSignIn", "Error parsing Google ID token", e)
                    Toast.makeText(context, "Failed to parse ID token", Toast.LENGTH_SHORT).show()
                    null
                }
            } else {
                Log.e("GoogleSignIn", "Unexpected credential type: ${credential.type}")
                Toast.makeText(context, "Invalid credential type", Toast.LENGTH_SHORT).show()
                null
            }

        } catch (e: GetCredentialException) {
            Log.e("GoogleSignIn", "Sign-in failed", e)
            Toast.makeText(context, "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    suspend fun authorizeCalendarAccess(context: Context): String? { //this is the one that will get you the access to calendar data
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(CalendarConstants.SCOPES)
            .requestOfflineAccess(CalendarConstants.WEB_CLIENT_ID)
            .build()

        return suspendCoroutine { continuation ->
            Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .addOnSuccessListener { result ->
                    continuation.resume(result.accessToken)
                }
                .addOnFailureListener { err ->
                    Log.e("GoogleSignIn", "Authorization failed: ${err.message}", err)
                    continuation.resume(null)
                }
        }
    }

    suspend fun performFullGoogleLogin(context: Context): GoogleAccount? { // this unites both of the above functions into one, so you call this thing and it will return the account
        val idToken = signIn(context)
        if (idToken == null) {
            Log.e("GoogleLogin", "Failed to get ID token.")
            return null
        }

        val accessToken = authorizeCalendarAccess(context)
        if (accessToken == null) {
            Log.e("GoogleLogin", "Failed to get access token.")
            return null
        }

        val account = GoogleAccount(
            email = "", // fill if you want from sign-in later
            displayName = "",
            idToken = idToken,
            accessToken = accessToken
        )

        saveAccount(account, context)
        Log.d("GoogleLogin", "Account saved with both tokens.")
        return account
    }







    fun saveAccount(account: GoogleAccount, context: Context) { //function, yes, it saves the account once logged in
        GoogleAccountRepository.addAccount(context, account)
    }

    fun generateSecureRandomNonce(byteLength: Int = 32): String { //security thing for google sign in to work
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}