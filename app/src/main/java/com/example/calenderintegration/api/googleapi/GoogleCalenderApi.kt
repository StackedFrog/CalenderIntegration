package com.example.calenderintegration.api.googleapi

import com.google.api.services.calendar.Calendar
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory

class GoogleCalenderApi {
    private val APPLICATION_NAME = "Calender"

    fun getCalendarService(credential: GoogleAccountCredential): Calendar {
        return Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}