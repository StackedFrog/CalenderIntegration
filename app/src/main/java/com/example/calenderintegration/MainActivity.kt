package com.example.calenderintegration

import android.os.Bundle

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.material3.*

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.example.calenderintegration.ui.auth.AuthViewModel
import com.example.calenderintegration.ui.auth.LoginScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint // Required for Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Use Hilt to get the ViewModel instance
                val authViewModel: AuthViewModel = hiltViewModel()

                // Call your LoginScreen composable
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        // For debugging: print or log something when login succeeds
                        Log.d("LoginScreen", "Login success triggered")
                    }
                )
            }
        }
    }
}





