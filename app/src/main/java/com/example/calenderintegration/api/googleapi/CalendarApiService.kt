package com.example.calenderintegration.api.googleapi


import android.content.Context
import android.util.Log
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.model.GoogleAccount
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object CalendarApiService {

    fun fetchCalendarData(context: Context, account: GoogleAccount, onResult: (List<Event>) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            .addHeader("Authorization", "Bearer ${account.accessToken}")
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string() // read once

                    if (response.code == 401) {
                        Log.w("CalendarAPI", "Token expired, refreshing for ${account.email}")
                        val refreshed = runBlocking {
                            GoogleSignIn.refreshAccessToken(context, account)
                        }
                        if (refreshed != null) {
                            fetchCalendarData(context, refreshed, onResult)
                            return@Thread
                        } else {
                            onResult(emptyList())
                            return@Thread
                        }
                    }

                    if (response.isSuccessful && bodyStr != null) {
                        val json = JSONObject(bodyStr)
                        val items = json.optJSONArray("items") ?: JSONArray()
                        val apiEvents = mutableListOf<Event>()

                        for (i in 0 until items.length()) {
                            val obj = items.getJSONObject(i)
                            val startObj = obj.optJSONObject("start")
                            val endObj = obj.optJSONObject("end")

                            // Distinguish between timed and all-day events explicitly
                            val startTime = when {
                                startObj?.has("dateTime") == true -> startObj.getString("dateTime")
                                startObj?.has("date") == true -> startObj.getString("date")
                                else -> "No time specified"
                            }

                            val endTime = when {
                                endObj?.has("dateTime") == true -> endObj.getString("dateTime")
                                endObj?.has("date") == true -> endObj.getString("date")
                                else -> "No time specified"
                            }

                            apiEvents.add(
                                Event(
                                    id = obj.optString("id", ""),
                                    summary = obj.optString("summary", ""),
                                    description = obj.optString("description", ""),
                                    start = startTime,
                                    end = endTime,
                                    location = obj.optString("location", ""),
                                    calendarEmail = account.email
                                )
                            )
                        }

                        onResult(apiEvents)
                    } else {
                        Log.e("CalendarAPI", "Failed to fetch: HTTP ${response.code} - $bodyStr")
                        onResult(emptyList())
                    }

                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "Failed to fetch events", e)
                onResult(emptyList())
            }
        }.start()
    }



    fun createCalendarEvent(
        context: Context,
        account: GoogleAccount,
        event: Event,
        onResult: (Boolean) -> Unit
    ) {
        val client = OkHttpClient()

        val jsonBody = JSONObject().apply {
            put("summary", event.summary)
            put("description", event.description)
            put("location", event.location)
            put("start", JSONObject().apply {
                put("dateTime", event.start)
                put("timeZone", "UTC")
            })
            put("end", JSONObject().apply {
                put("dateTime", event.end)
                put("timeZone", "UTC")
            })
        }

        val body = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            .addHeader("Authorization", "Bearer ${account.accessToken}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.code == 401) {
                        Log.w("CalendarAPI", "Token expired, refreshing for ${account.email}")
                        val refreshed = runBlocking {
                            GoogleSignIn.refreshAccessToken(context, account)
                        }
                        if (refreshed != null) {
                            createCalendarEvent(context, refreshed, event, onResult)
                            return@Thread
                        } else {
                            onResult(false)
                            return@Thread
                        }
                    }

                    if (response.isSuccessful) {
                        Log.d("CalendarAPI", "Event created successfully for ${account.email}")
                        onResult(true)
                    } else {
                        Log.e("CalendarAPI", "Failed: HTTP ${response.code} - ${response.body?.string()}")
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "Exception creating event", e)
                onResult(false)
            }
        }.start()
    }





    fun deleteCalendarEvent(
        context: Context,
        account: GoogleAccount,
        eventId: String,
        onResult: (Boolean) -> Unit
    ) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId")
            .addHeader("Authorization", "Bearer ${account.accessToken}")
            .delete()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string()

                    if (response.code == 401) {
                        Log.w("CalendarAPI", "Token expired, refreshing for ${account.email}")
                        val refreshed = runBlocking {
                            GoogleSignIn.refreshAccessToken(context, account)
                        }
                        if (refreshed != null) {
                            deleteCalendarEvent(context, refreshed, eventId, onResult)
                            return@Thread
                        } else {
                            onResult(false)
                            return@Thread
                        }
                    }

                    if (response.isSuccessful) {
                        Log.d("CalendarAPI", "Event deleted successfully for ${account.email}")
                        onResult(true)
                    } else {
                        Log.e("CalendarAPI", "Delete failed: HTTP ${response.code} - $bodyStr")
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "Exception deleting event", e)
                onResult(false)
            }
        }.start()
    }



    fun updateCalendarEvent(
        context: Context,
        account: GoogleAccount,
        event: Event,
        onResult: (Boolean) -> Unit
    ) {
        val client = OkHttpClient()

        val jsonBody = JSONObject().apply {
            put("summary", event.summary)
            put("description", event.description)
            put("location", event.location)
            put("start", JSONObject().apply {
                put("dateTime", event.start)
                put("timeZone", "UTC")
            })
            put("end", JSONObject().apply {
                put("dateTime", event.end)
                put("timeZone", "UTC")
            })
        }

        val body = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events/${event.id}")
            .addHeader("Authorization", "Bearer ${account.accessToken}")
            .addHeader("Content-Type", "application/json")
            .put(body) // <— PUT is used for updates
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string()

                    if (response.code == 401) {
                        Log.w("CalendarAPI", "Token expired, refreshing for ${account.email}")
                        val refreshed = runBlocking {
                            GoogleSignIn.refreshAccessToken(context, account)
                        }
                        if (refreshed != null) {
                            updateCalendarEvent(context, refreshed, event, onResult)
                            return@Thread
                        } else {
                            onResult(false)
                            return@Thread
                        }
                    }

                    if (response.isSuccessful) {
                        Log.d("CalendarAPI", "Event updated successfully for ${account.email}")
                        onResult(true)
                    } else {
                        Log.e("CalendarAPI", "Failed to update: HTTP ${response.code} - $bodyStr")
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "Exception updating event", e)
                onResult(false)
            }
        }.start()
    }
}