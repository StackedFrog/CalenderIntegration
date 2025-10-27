package com.example.calenderintegration.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderintegration.api.Zohoapi.ZohoAuthManager
import com.example.calenderintegration.api.Zohoapi.ZohoCalendarManager
import com.example.calenderintegration.api.Zohoapi.ZohoEventManager
import com.example.calenderintegration.model.calendarEvent
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var authManager: ZohoAuthManager
    private var calendarManager: ZohoCalendarManager? = null
    private var eventManager: ZohoEventManager? = null

    private var calendarUid: String? = null
    private val redirectUri = "com.myzoho://oauth2redirect"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(32, 32, 32, 32)
        }

        progressBar = ProgressBar(this).apply { visibility = View.GONE }
        authManager = ZohoAuthManager()

        val loginButton = Button(this).apply {
            text = "Login to Zoho"
            setOnClickListener { showLoginWebView() }
        }

        rootLayout.addView(loginButton)
        rootLayout.addView(progressBar)
        setContentView(rootLayout)
    }

    private fun showLoginWebView() {
        rootLayout.removeAllViews()
        progressBar.visibility = View.VISIBLE
        rootLayout.addView(progressBar)

        val webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null && url.startsWith(redirectUri)) {
                        val uri = Uri.parse(url)
                        val authCode = uri.getQueryParameter("code")
                        if (!authCode.isNullOrEmpty()) {
                            exchangeZohoToken(authCode)
                            rootLayout.removeView(this@apply)
                        }
                        return true
                    }
                    return false
                }
            }
        }

        webView.loadUrl(authManager.getAuthUrl())
        rootLayout.addView(webView)
    }

    private fun exchangeZohoToken(authCode: String) {
        runOnUiThread { progressBar.visibility = View.VISIBLE }

        authManager.exchangeToken(authCode,
            onSuccess = {
                calendarManager = ZohoCalendarManager(authManager)
                eventManager = ZohoEventManager(authManager)
                fetchCalendarList()
            },
            onError = { e ->
                runOnUiThread {
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AuthError", "Token exchange failed", e)
                }
            })
    }

    private fun fetchCalendarList() {
        authManager.ensureValidToken(
            onValid = { token ->
                calendarManager?.fetchCalendars(
                    onSuccess = { calendars ->
                        if (calendars.isNotEmpty()) {
                            calendarUid = calendars.first()
                            runOnUiThread { showMainMenu() }
                        } else runOnUiThread {
                            Toast.makeText(this, "No calendars found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { e ->
                        runOnUiThread {
                            Toast.makeText(this, "Failed to fetch calendars", Toast.LENGTH_SHORT).show()
                            Log.e("CalendarError", "Failed to load calendars", e)
                        }
                    }
                )
            },
            onError = { e ->
                runOnUiThread {
                    Toast.makeText(this, "Token invalid: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showMainMenu() {
        rootLayout.removeAllViews()
        val createButton = Button(this).apply {
            text = "Create Event"
            setOnClickListener { showCreateEventForm() }
        }
        val showButton = Button(this).apply {
            text = "Show Events"
            setOnClickListener {
                rootLayout.removeAllViews()
                progressBar.visibility = View.VISIBLE
                rootLayout.addView(progressBar)
                fetchEvents()
            }
        }
        rootLayout.addView(createButton)
        rootLayout.addView(showButton)
    }

    private fun showCreateEventForm() {
        rootLayout.removeAllViews()
        val titleInput = EditText(this).apply { hint = "Event Title" }
        val descInput = EditText(this).apply { hint = "Description" }
        val startBtn = Button(this).apply { text = "Select Start Time" }
        val endBtn = Button(this).apply { text = "Select End Time" }
        val createBtn = Button(this).apply {
            text = "Create Event"
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
        }

        var startDateTime: Calendar? = null
        var endDateTime: Calendar? = null

        startBtn.setOnClickListener {
            pickDateTime { cal ->
                startDateTime = cal
                startBtn.text = "Start: ${formatDisplayTime(cal.time)}"
            }
        }

        endBtn.setOnClickListener {
            pickDateTime { cal ->
                endDateTime = cal
                endBtn.text = "End: ${formatDisplayTime(cal.time)}"
            }
        }

        createBtn.setOnClickListener {
            if (startDateTime == null || endDateTime == null || titleInput.text.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authManager.ensureValidToken(
                onValid = { token ->
                    eventManager?.createEvent(
                        calendarUid!!,
                        titleInput.text.toString(),
                        descInput.text.toString(),
                        startDateTime!!.time,
                        endDateTime!!.time,
                        onSuccess = {
                            runOnUiThread {
                                Toast.makeText(this, "Event created", Toast.LENGTH_SHORT).show()
                                fetchEvents()
                            }
                        },
                        onError = { e ->
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "Failed to create event: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("CreateEvent", "Error", e)
                            }
                        }
                    )
                },
                onError = { e ->
                    runOnUiThread {
                        Toast.makeText(this, "Token invalid: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        rootLayout.apply {
            addView(titleInput)
            addView(descInput)
            addView(startBtn)
            addView(endBtn)
            addView(createBtn)
        }
    }

    private fun fetchEvents() {
        authManager.ensureValidToken(
            onValid = { token ->
                eventManager?.fetchEvents(
                    calendarUid!!,
                    "user@zoho.com",
                    onSuccess = { events -> runOnUiThread { displayEvents(events) } },
                    onError = { e ->
                        runOnUiThread {
                            Toast.makeText(this, "Failed to fetch events: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            onError = { e ->
                runOnUiThread { Toast.makeText(this, "Token invalid: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        )
    }

    private fun displayEvents(events: List<calendarEvent>) {
        rootLayout.removeAllViews()

        events.forEach { ev ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#E8F0FE"))
            }

            val tv = TextView(this).apply {
                text = """
                    ${ev.summary}
                    ${ev.description}
                    Start: ${formatZohoDisplayTime(ev.start)}
                    End: ${formatZohoDisplayTime(ev.end)}
                """.trimIndent()
            }

            val deleteBtn = Button(this).apply {
                text = "Delete"
                setBackgroundColor(Color.RED)
                setTextColor(Color.WHITE)
                setOnClickListener { deleteEvent(ev.id, ev.etag) }
            }

            layout.addView(tv)
            layout.addView(deleteBtn)
            rootLayout.addView(layout)
        }

        val back = Button(this).apply {
            text = "Back"
            setOnClickListener { showMainMenu() }
        }
        rootLayout.addView(back)
    }

    private fun deleteEvent(eventUid: String, etag: String) {
        authManager.ensureValidToken(
            onValid = { token ->
                eventManager?.deleteEvent(
                    calendarUid!!,
                    eventUid,
                    etag,
                    onSuccess = {
                        runOnUiThread {
                            Toast.makeText(this, "Event Deleted", Toast.LENGTH_SHORT).show()
                            fetchEvents()
                        }
                    },
                    onError = {
                        runOnUiThread { Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show() }
                    }
                )
            },
            onError = { e -> runOnUiThread { Toast.makeText(this, "Token invalid: ${e.message}", Toast.LENGTH_SHORT).show() } }
        )
    }

    private fun pickDateTime(onPicked: (Calendar) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        val cal = Calendar.getInstance()
                        cal.set(year, month, day, hour, minute)
                        onPicked(cal)
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatZohoDisplayTime(zohoTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(zohoTime)
            SimpleDateFormat("EEE, dd MMM yyyy\nHH:mm", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            zohoTime
        }
    }

    private fun formatDisplayTime(date: Date): String =
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(date)
}
