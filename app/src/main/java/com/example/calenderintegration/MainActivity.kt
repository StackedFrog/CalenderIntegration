package com.example.calenderintegration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.calenderintegration.ui.navigation.MainScreen
import com.example.calenderintegration.ui.theme.CalenderIntegrationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalenderIntegrationTheme {
                MainScreen()
            }
        }
    }


}

@Composable
fun App2() {
    Text("Greeeeetings")
}