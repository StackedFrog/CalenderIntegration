package com.example.calenderintegration.api.googleapi


import android.content.Context
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.example.calenderintegration.model.Event
import com.example.calenderintegration.model.GoogleAccount
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object CalendarApiService {

    fun requestAuthorizationToken(// do not use this at all
    context: Context,
    onTokenReceived: (String?) -> Unit,
    onResolutionRequired: (IntentSenderRequest) -> Unit
    ) {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(CalendarConstants.SCOPES)
            .requestOfflineAccess(CalendarConstants.WEB_CLIENT_ID)
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    val request = IntentSenderRequest.Builder(pendingIntent!!.intentSender).build()
                    onResolutionRequired(request)  // handled by Activity
                } else {
                    onTokenReceived(result.accessToken)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CalendarAuth", "Authorization failed: ${e.message}", e)
                onTokenReceived(null)
            }
    }







    fun fetchCalendarData(account: GoogleAccount, onResult: (List<Event>) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            .addHeader("Authorization", "Bearer ${account.accessToken}")
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val json = JSONObject(body)
                        val items = json.optJSONArray("items") ?: JSONArray()
                        val apiEvents = mutableListOf<Map<String, Any?>>()
                        for (i in 0 until items.length()) {
                            val obj = items.getJSONObject(i)
                            val map = mutableMapOf<String, Any?>()
                            obj.keys().forEach { key -> map[key] = obj.opt(key) }
                            apiEvents.add(map)
                        }

                        val appEvents = apiEvents.map {
                            val startObj = it["start"]
                            val endObj = it["end"]

                            val startTime = when (startObj) {
                                is JSONObject -> startObj.optString("dateTime", startObj.optString("date", ""))
                                is Map<*, *> -> startObj["dateTime"]?.toString() ?: startObj["date"]?.toString() ?: ""
                                else -> ""
                            }

                            val endTime = when (endObj) {
                                is JSONObject -> endObj.optString("dateTime", endObj.optString("date", ""))
                                is Map<*, *> -> endObj["dateTime"]?.toString() ?: endObj["date"]?.toString() ?: ""
                                else -> ""
                            }

                            Event(
                                id = it["id"]?.toString() ?: "",
                                summary = it["summary"]?.toString() ?: "",
                                description = it["description"]?.toString() ?: "",
                                start = startTime,
                                end = endTime,
                                location = it["location"]?.toString() ?: "",
                                calendarEmail = account.email


                            )
                        }

                        onResult(appEvents)
                    } else {
                        Log.e("CalendarAPI", "HTTP ${response.code}: ${response.body?.string()}")
                        onResult(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "Failed to fetch events", e)
                onResult(emptyList())
            }
        }.start()
    }




    fun createCalendarEvent(account: GoogleAccount, event: Event, onResult: (Boolean) -> Unit) {
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





    fun deleteCalendarEvent(account: GoogleAccount, eventId: String, onResult: (Boolean) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId")
            .addHeader("Authorization", "Bearer ${account.accessToken}")
            .delete()
            .build()

        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("CalendarAPI", "Event deleted successfully for ${account.email}")
                        onResult(true)
                    } else {
                        Log.e("CalendarAPI", "Delete failed: HTTP ${response.code} - ${response.body?.string()}")
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarAPI", "Exception deleting event", e)
                onResult(false)
            }
        }.start()
    }



}