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
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launcher for Google Sign-In IntentSender
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // You can handle post-sign-in result here if needed
    }

    // Observe login state and navigate when successful
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
        verticalArrangement = Arrangement.Top, // Centers content vertically
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign Up / Log In",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = 45.dp),
                fontSize = 30.sp
            )
        }
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
                    color = MaterialTheme.colorScheme.surfaceDim,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            // First button slightly higher
            Button(
                onClick = {
                    coroutineScope.launch {
                        authViewModel.logIn(
                            context = context,
                            startIntentSender = { intentSenderRequest ->
                                launcher.launch(intentSenderRequest)
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-150).dp) // move upward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)        // fixed height instead of fillMaxHeight
            ) {
                Text(text = "Sign in with Google",
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            // Second button slightly lower
            Button(
                onClick = { /* log-in */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 0.dp)    // move downward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text(text = "Sign in with Zoho",
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }


            // Third button slightly lower
            Button(
                onClick = { /* log-in */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 150.dp)    // move downward from center
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text(text = "Sign in with Outlook",
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        // Bottom decorative box
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-60).dp)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.10f)
                .background(
                    color = Color(0xFFBBDEFB),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {}
    }
}






