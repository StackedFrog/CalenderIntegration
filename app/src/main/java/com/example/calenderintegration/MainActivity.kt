package com.example.calenderintegration

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.calenderintegration.api.googleapi.CalendarApiService.createCalendarEvent
import com.example.calenderintegration.api.googleapi.CalendarApiService.deleteCalendarEvent
import com.example.calenderintegration.api.googleapi.CalendarApiService.fetchCalendarData
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.googleapi.GoogleSignIn

import com.example.calenderintegration.model.Event

import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import java.time.Instant


class MainActivity : ComponentActivity() {

    // pop up that asks for permissions to get google calendar data - needed cuz without it u can not grant access to the app to fetch calendar data and so on
    private val startAuthorizationIntent = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val data = result.data ?: return@registerForActivityResult
        val authClient = Identity.getAuthorizationClient(this)
        val authResult = authClient.getAuthorizationResultFromIntent(data)
        val token = authResult.accessToken
        if (token != null) Log.d("GoogleAuth", "Access token (interactive): $token")
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            var accounts by remember { mutableStateOf(GoogleAccountRepository.loadAccounts(context)) }
            var events by remember { mutableStateOf<List<Event>>(emptyList()) }

            MainScreen(
                accounts = accounts,
                events = events,



                onSignInClick = {
                    lifecycleScope.launch {

                        //performs the full sign in
                        val account = GoogleSignIn.performFullGoogleLogin(
                            context = this@MainActivity,
                            startIntentSender = { req -> startAuthorizationIntent.launch(req) } // the same val from above being actually called
                        )

                        //load account function found in GoogleAccountRepository
                        if (account != null) {
                            accounts = GoogleAccountRepository.loadAccounts(context)
                        }

                    }
                },

                //use fetch function to get the events
                onFetchEvents = { account ->
                    fetchCalendarData(context, account) { fetched ->
                        events = fetched
                    }
                }
            )
        }
    }
}



@Composable
fun MainScreen(
    accounts: List<GoogleAccount>,
    events: List<Event>,
    onSignInClick: () -> Unit,
    onFetchEvents: (GoogleAccount) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onSignInClick) {
                Icon(
                    painter = painterResource(id = R.drawable.siwg_button),
                    contentDescription = "Sign in"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2F2F2))
                .padding(16.dp)
        ) {
            Text("Saved Google Accounts:", fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))

            if (accounts.isEmpty()) {
                Text("No accounts found yet.", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(accounts) { account ->
                        Card(
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(account.displayName.ifEmpty { "(No name)" }, fontSize = 16.sp)
                                Text(account.email, color = Color.DarkGray, fontSize = 13.sp)
                                Spacer(Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    //ui button for fetching
                                    Button(
                                        onClick = { onFetchEvents(account) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Text("Fetch", fontSize = 12.sp)
                                    }

                                    //ui button for the create event function
                                    Button(
                                        onClick = {
                                            //the time is NOW... to test. Start and End time for the event construction
                                            val now = Instant.now()
                                            val start = now.plusSeconds(60)
                                            val end = start.plusSeconds(3600)

                                            //create hardcoded event for testing
                                            val event = Event(
                                                id = "",
                                                calendarEmail = account.email,
                                                summary = "Test Event",
                                                description = "Created from app",
                                                location = "Online",
                                                start = start.toString(),
                                                end = end.toString()
                                            )


                                            //create calendar function then fetch events again
                                            createCalendarEvent(context, account, event) { success ->
                                                val msg = if (success) "Event created!" else "Event creation failed."
                                                Log.d("CalendarAPI", msg)
                                                Handler(Looper.getMainLooper()).post {
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Text("Create", fontSize = 12.sp)
                                    }

                                    //ui button to delete the last event
                                    Button(
                                        onClick = {
                                            if (events.isNotEmpty()) {
                                                val lastEvent = events.last()
                                                if (lastEvent.id.isNotEmpty()) {

                                                    //delete calendar function then fetch events again
                                                    deleteCalendarEvent(context, account, lastEvent.id) { success ->
                                                        val msg = if (success) "Deleted last!" else "Delete failed."
                                                        Log.d("CalendarAPI", msg)
                                                        Handler(Looper.getMainLooper()).post {
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                            if (success) {
                                                                // Refresh events from Google Calendar
                                                                onFetchEvents(account)
                                                            }
                                                        }
                                                    }



                                                } else {
                                                    Toast.makeText(context, "No event ID found.", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "No events to delete.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Text("Delete", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }


// ui stuff to display the events
            Spacer(Modifier.height(12.dp))
            Text("Fetched Calendar Events:", fontSize = 18.sp)

            if (events.isEmpty()) {
                Text("No events fetched.", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(events) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                Text("Summary: ${event.summary}", fontSize = 15.sp, color = Color.Black)
                                Text("Description: ${event.description}", fontSize = 13.sp, color = Color.DarkGray)
                                Text("Location: ${event.location.ifEmpty { "(none)" }}", fontSize = 13.sp)
                                Text("Start: ${event.start}", fontSize = 13.sp)
                                Text("End: ${event.end}", fontSize = 13.sp)
                                Text("Event ID: ${event.id.ifEmpty { "(none)" }}", fontSize = 12.sp)
                                Text("Calendar Email: ${event.calendarEmail}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

