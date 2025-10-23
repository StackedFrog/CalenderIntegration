package com.example.calenderintegration.repository

import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.OutlookAccount
import jakarta.inject.Inject

class AccountsRepository @Inject constructor() {
    fun getGoogleAccounts(): List<GoogleAccount> {
        // return a list of all currently logged in google accounts
        return emptyList()
    }

    fun getOutlookAccounts(): List<OutlookAccount> {
        // return a list of all currently logged in outlook accounts
        // or whatever we end up using
        return emptyList()
    }
}