package com.example.calenderintegration





import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
