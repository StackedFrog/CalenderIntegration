package com.example.calenderintegration.ui.auth

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.calenderintegration.ui.auth.AuthViewModel

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
        contract = StartIntentSenderForResult()
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
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Sign Up / Log In",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Blue,
            modifier = Modifier.padding(top = 50.dp),
            fontSize = 30.sp
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main background container
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
            // ✅ Google Sign-In button
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
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-150).dp)
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Use Google Account")
            }

            // Zoho button
            Button(
                onClick = { /* TODO: implement Zoho login */ },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 0.dp)
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Use Zoho Account")
            }

            // Outlook button
            Button(
                onClick = { /* TODO: implement Outlook login */ },
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 150.dp)
                    .fillMaxWidth(0.75f)
                    .height(70.dp)
            ) {
                Text("Use Outlook Account")
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






