package com.example.calenderintegration.ui


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.calenderintegration.R
import com.example.calenderintegration.api.googleapi.GoogleSignIn
import com.example.calenderintegration.api.googleapi.GoogleSignIn.performFullGoogleLogin
import kotlinx.coroutines.launch



object GoogleSigninButton {
    @Composable
    fun ButtonUI() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val onClick: () -> Unit = {

            coroutineScope.launch {
                val account = performFullGoogleLogin(context)
                if (account != null) {
                    Log.d("GoogleLogin", "Success: ${account.email}")
                }
            }
        }

        Box(
            modifier = Modifier.Companion
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter

        ) {
            Image(
                painter = painterResource(id = R.drawable.siwg_button),
                contentDescription = "Sign in",
                modifier = Modifier.Companion
                    .width(220.dp)
                    .height(60.dp)
                    .padding(bottom = 40.dp)
                    .clickable(onClick = onClick)
            )
        }
    }
}