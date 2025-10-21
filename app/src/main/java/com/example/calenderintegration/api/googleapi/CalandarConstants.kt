package com.example.calenderintegration.api.googleapi

import com.google.android.gms.common.api.Scope


object CalendarConstants {

    val SCOPES = listOf( // Needed to get access to the  google calendar as in permission
        Scope("https://www.googleapis.com/auth/calendar")
    )

    //used for authorization google "small page with sign in on the ui"
    const val WEB_CLIENT_ID = "543218373041-9i6rmg7ad23jr3d8vati18f0ifg70mqs.apps.googleusercontent.com"
}