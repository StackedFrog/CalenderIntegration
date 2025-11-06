    package com.example.calenderintegration.api.zohoapi

    import android.util.Log
    import okhttp3.*
    import org.json.JSONObject
    import java.io.IOException

    class ZohoCalendarManager(private val authManager: ZohoAuthManager) {

        private val client = OkHttpClient()

        fun fetchCalendars(
            onSuccess: (List<String>) -> Unit,
            onError: (Exception) -> Unit
        ) {
            authManager.ensureValidToken(
                onValid = { token ->
                    val request = Request.Builder()
                        .url("https://calendar.zoho.eu/api/v1/calendars")
                        .addHeader("Authorization", "Zoho-oauthtoken $token")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) = onError(e)
                        override fun onResponse(call: Call, response: Response) {
                            val body = response.body?.string()
                            val calendars = JSONObject(body ?: "")
                                .optJSONArray("calendars") ?: return
                            val list = mutableListOf<String>()
                            for (i in 0 until calendars.length()) {
                                list.add(calendars.getJSONObject(i).optString("uid"))
                            }
                            Log.d("ZohoCalendar", "Fetched calendars: $list")
                            onSuccess(list)
                        }
                    })
                },
                onError = onError
            )
        }
    }