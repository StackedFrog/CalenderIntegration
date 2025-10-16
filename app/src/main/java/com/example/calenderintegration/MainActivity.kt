package com.example.calenderintegration

import android.content.ContentValues.TAG
import android.content.Context
import android.credentials.GetCredentialException
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import java.util.Base64
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.calenderintegration.api.googleapi.CalendarConstants
import com.example.calenderintegration.ui.theme.CalenderIntegrationTheme
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {


    private val startAuthorizationIntent = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        try {
            val authorizationResult = Identity.getAuthorizationClient(this)
                .getAuthorizationResultFromIntent(activityResult.data)
            val token = authorizationResult.accessToken
            Log.d("CalendarAuth", "✅ Token from consent: $token")
            fetchCalendarData(token) // ✅ visible here
        } catch (e: Exception) {
            Log.e("CalendarAuth", "❌ Failed to retrieve token", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {




        super.onCreate(savedInstanceState)

        //replace with your own web client ID from Google Cloud Console
        val webClientId = CalendarConstants.WEB_CLIENT_ID



        setContent {
            //ExampleTheme - this is derived from the name of the project not any added library
            //e.g. if this project was named "Testing" it would be generated as TestingTheme
            CalenderIntegrationTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        //This will trigger on launch
                        BottomSheet(webClientId)

                        //This requires the user to press the button
                        ButtonUI(webClientId)
                    }
                }
            }
        }
    }

    fun requestCalendarAccessAndFetchData() {
        val requestedScopes = CalendarConstants.SCOPES

        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .requestOfflineAccess(CalendarConstants.WEB_CLIENT_ID)
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    // User must approve access → show consent screen
                    val pendingIntent = result.pendingIntent
                    startAuthorizationIntent.launch(
                        IntentSenderRequest.Builder(pendingIntent!!.intentSender).build()
                    )
                } else {
                    // Access already granted
                    val token = result.accessToken
                    Log.d("CalendarAuth", "✅ Access token: $token")
                    fetchCalendarData(token)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CalendarAuth", "❌ Authorization failed: ${e.message}", e)
            }
    }

    private fun fetchCalendarData(accessToken: String?) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: "Empty response"
                    if (response.isSuccessful) {
                        Log.d("CalendarAPI", "📅 Calendar data:\n$body")
                    } else {
                        Log.e("CalendarAPI", "❌ ${response.code} - $body")
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "❌ Failed to fetch calendar data", e)
            }
        }.start()
    }


}













suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
    val credentialManager = CredentialManager.create(context)
    val failureMessage = "Sign in failed!"
    var e: Exception? = null
    //using delay() here helps prevent NoCredentialException when the BottomSheet Flow is triggered
    //on the initial running of our app
    delay(250)
    try {
        // The getCredential is called to request a credential from Credential Manager.
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        Log.i(TAG, result.toString())

        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "(☞ﾟヮﾟ)☞  Sign in Successful!  ☜(ﾟヮﾟ☜)")
        if (context is MainActivity) {
            context.requestCalendarAccessAndFetchData()
        }


    } catch (e: GetCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Failure getting credentials", e)

    } catch (e: GoogleIdTokenParsingException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Issue with parsing received GoogleIdToken", e)

    } catch (e: NoCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": No credentials found", e)
        return e

    } catch (e: GetCredentialCustomException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Issue with custom credential request", e)

    } catch (e: GetCredentialCancellationException) {
        Toast.makeText(context, ": Sign-in cancelled", Toast.LENGTH_SHORT).show()
        Log.e(TAG, failureMessage + ": Sign-in was cancelled", e)
    }
    return e
}


@Composable
fun BottomSheet(webClientId: String) {
    val context = LocalContext.current

    // LaunchedEffect is used to run a suspend function when the composable is first launched.
    LaunchedEffect(Unit) {
        // Create a Google ID option with filtering by authorized accounts enabled.
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        // Create a credential request with the Google ID option.
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Attempt to sign in with the created request using an authorized account
        val e = signIn(request, context)
        // If the sign-in fails with NoCredentialException,  there are no authorized accounts.
        // In this case, we attempt to sign in again with filtering disabled.
        if (e is NoCredentialException) {
            val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(generateSecureRandomNonce())
                .build()

            val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionFalse)
                .build()

            //We will build out this function in a moment
            signIn(requestFalse, context)
        }
    }
}

//This function is used to generate a secure nonce to pass in with our request
fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}

@Composable
fun ButtonUI(webClientId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            signIn(request, context)
        }
    }
    Image(
        painter = painterResource(id = R.drawable.siwg_button),
        contentDescription = "",
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = onClick)
    )
}


