package com.example.calenderintegration.api.zohoapi

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZohoAuthManager @Inject constructor() {

    private val client = OkHttpClient()

    private val clientId = "1000.MVH1R2JHRROLU7YK9K132SC7VG66JG"
    private val clientSecret = "1792ef40ee47ee60835200b8ca30fb43de68781b08"
    private val redirectUri = "com.myzoho://oauth2redirect"

    /**
     * Pablo 1/11: Changed these three to public because apparently we need to access them
     */
    var accessToken: String? = null
    var refreshToken: String? = null
    var expiryTime: Long = 0 // Unix millis when access token expires



    fun getAuthUrl(): String {
        return "https://accounts.zoho.eu/oauth/v2/auth" +
                "?scope=ZohoCalendar.calendar.ALL,ZohoCalendar.event.ALL" +
                "&client_id=$clientId" +
                "&response_type=code" +
                "&access_type=offline" +
                "&redirect_uri=$redirectUri"
    }

    fun exchangeToken(
        authCode: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("redirect_uri", redirectUri)
            .add("code", authCode)
            .build()

        val request = Request.Builder()
            .url("https://accounts.zoho.eu/oauth/v2/token")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError(e)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val json = JSONObject(it.body?.string() ?: "")
                    accessToken = json.optString("access_token")
                    refreshToken = json.optString("refresh_token")
                    val expiresIn = json.optLong("expires_in", 3600) // default 1 hour
                    expiryTime = System.currentTimeMillis() + expiresIn * 1000
                    Log.d("ZohoAuth", "Access token received: $accessToken")
                    Log.d("ZohoAuth", "Refresh token received: $refreshToken")
                    onSuccess(accessToken!!)
                }
            }
        })
    }

    private fun refreshAccessToken(
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (refreshToken == null) {
            onError(Exception("No refresh token available"))
            return
        }

        val formBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("refresh_token", refreshToken!!)
            .build()

        val request = Request.Builder()
            .url("https://accounts.zoho.eu/oauth/v2/token")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError(e)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val json = JSONObject(it.body?.string() ?: "")
                    accessToken = json.optString("access_token")
                    val expiresIn = json.optLong("expires_in", 3600)
                    expiryTime = System.currentTimeMillis() + expiresIn * 1000
                    if (accessToken.isNullOrEmpty()) onError(Exception("Failed to refresh token"))
                    else {
                        Log.d("ZohoAuth", "Access token refreshed successfully")
                        onSuccess(accessToken!!)
                    }
                }
            }
        })
    }

    // Automatic refresh if expired
    fun ensureValidToken(
        onValid: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val now = System.currentTimeMillis()
        if (accessToken != null && now < expiryTime) {
            // Token is still valid
            onValid(accessToken!!)
        } else if (refreshToken != null) {
            // Token expired, refresh it
            refreshAccessToken(onValid, onError)
        } else {
            onError(Exception("No access token available and no refresh token"))
        }
    }
}