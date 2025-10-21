package com.example.calenderintegration.repository

import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.OutlookAccount
import jakarta.inject.Inject

class AccountsRepository @Inject constructor() {
    fun getGoogleAccounts(): List<GoogleAccount> {
        return emptyList()
    }

    fun getOutlookAccounts(): List<OutlookAccount> {
        return emptyList()
    }
}