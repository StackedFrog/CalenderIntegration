package com.example.calenderintegration.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calenderintegration.repository.EventRepository
import kotlinx.coroutines.launch


// TODO: Implement google api from the backend
@Composable
fun LoginScreen (
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()



    // This launcher handles the Google Sign-In UI
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        // The result is handled internally by the Google Identity services,
        // so you don't need to add specific logic here. The `logIn` suspend
        // function in your ViewModel will simply resume its execution.
    }



    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp), // Adds padding around the screen,

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Centers content vertically
    ) {
        Text(
            text = "Sign Up / Log In",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Blue, // or any color you want
            modifier = Modifier.padding(top = 50.dp),
            fontSize = 30.sp
        )

    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main background box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.60f)
                .background(
                    color = Color(0xFFBBDEFB),
                    shape = RoundedCornerShape(25.dp)
                )
        ) {
            // First button slightly higher
            Button(
                onClick = {
                    coroutineScope.launch {
                    authViewModel.logIn(
                        context = context,
                        startIntentSender = { intentSenderRequest ->
                            intentSenderLauncher.launch(intentSenderRequest)
                        }
                    )
                    }
                          },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-150).dp) // move upward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)        // fixed height instead of fillMaxHeight
            ) {
                Text("Use Google Account")
            }

            // Second button slightly lower
            Button(
                onClick = { /* log-in */ },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -75.dp)    // move downward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Use Zoho Account")
            }


            // Third button slightly lower
            Button(
                onClick = { /* log-in */ },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 0.dp)    // move downward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Use Outlook Account")
            }


            // Fourth button to log out
            Button(
                onClick = {
                    coroutineScope.launch {
                        authViewModel.logOut(context)
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 75.dp)    // move downward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Force log out")
            }

            // Fifth button to fetch stuff
            Button(
                onClick = {
                    authViewModel.fetchAndLogEvents(context)
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 150.dp)    // move downward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Fetch and log events")
            }



        }


        //Followed Canva idk
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-60).dp) // move it slightly upward (negative = up)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.10f)
                .background(
                    color = Color(0xFFBBDEFB),
                    shape = RoundedCornerShape(18.dp)
                )
        ){}//Button maybe? Idk i followed canva on this one


    }




}





