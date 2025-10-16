package com.example.calenderintegration.api.googleapi

import com.google.android.gms.common.api.Scope


object CalendarConstants {
    // ✅ Use Scope objects instead of raw Strings
    val SCOPES = listOf(
        Scope("https://www.googleapis.com/auth/calendar")
    )
    const val WEB_CLIENT_ID = "543218373041-9i6rmg7ad23jr3d8vati18f0ifg70mqs.apps.googleusercontent.com"
}