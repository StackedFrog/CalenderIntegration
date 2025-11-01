package com.example.calenderintegration.api.zohoapi

import android.net.Uri
import android.util.Log
import com.example.calenderintegration.model.Event
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ZohoEventManager(private val authManager: ZohoAuthManager) {

    private val client = OkHttpClient()

    // Fetch events
    fun fetchEvents(
        calendarUid: String,
        calendarEmail: String,
        onSuccess: (List<Event>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        authManager.ensureValidToken(
            onValid = { token ->
                val url = "https://calendar.zoho.eu/api/v1/calendars/$calendarUid/events"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Zoho-oauthtoken $token")
                    .get()
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) = onError(e)
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: ""
                        val eventsJson = JSONObject(body).optJSONArray("events") ?: return
                        val events = mutableListOf<Event>()
                        for (i in 0 until eventsJson.length()) {
                            val ev = eventsJson.getJSONObject(i)
                            val dateAndTime = ev.optJSONObject("dateandtime")
                            events.add(
                                Event(
                                    id = ev.optString("uid"),
                                    summary = ev.optString("title"),
                                    description = ev.optString("description"),
                                    start = dateAndTime?.optString("start") ?: "",
                                    end = dateAndTime?.optString("end") ?: "",
                                    calendarEmail = calendarEmail,
                                    location = ev.optString("location", ""),
                                    etag = ev.optString("etag")
                                )
                            )
                        }
                        Log.d("ZohoEvent", "Fetched ${events.size} events")
                        onSuccess(events)
                    }
                })
            },
            onError = onError
        )
    }

    // Create event
    fun createEvent(
        calendarUid: String,
        summary: String,
        description: String,
        start: Date,
        end: Date,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        authManager.ensureValidToken(
            onValid = { token ->
                val formatter = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
                formatter.timeZone = TimeZone.getTimeZone("UTC")

                val eventJson = JSONObject().apply {
                    put("title", summary)
                    put("description", description)
                    put("dateandtime", JSONObject().apply {
                        put("start", formatter.format(start))
                        put("end", formatter.format(end))
                        put("timezone", "Europe/Berlin")
                    })
                    put("isallday", false)
                }

                val url =
                    "https://calendar.zoho.eu/api/v1/calendars/$calendarUid/events?eventdata=${Uri.encode(eventJson.toString())}"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Zoho-oauthtoken $token")
                    .post(RequestBody.create(null, ByteArray(0)))
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) = onError(e)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) onSuccess()
                        else onError(Exception("Create failed with code ${response.code}"))
                    }
                })
            },
            onError = onError
        )
    }

    // Delete event
    fun deleteEvent(
        calendarUid: String,
        eventId: String,
        etag: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        authManager.ensureValidToken(
            onValid = { token ->
                val url = "https://calendar.zoho.eu/api/v1/calendars/$calendarUid/events/$eventId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Zoho-oauthtoken $token")
                    .addHeader("etag", etag)
                    .delete()
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) = onError(e)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) onSuccess()
                        else onError(Exception("Delete failed with code ${response.code}"))
                    }
                })
            },
            onError = onError
        )
    }
}