package com.example.calenderintegration.repository

import android.content.Context
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.OutlookAccount
import jakarta.inject.Inject

class AccountsRepository @Inject constructor(
    private val accountRepository: GoogleAccountRepository
)
{

    /**
     *
     * Retrieves the Google Accounts that are saved.
     * Returns null if no accounts are found.
     */
    fun getGoogleAccounts(context : Context): List<GoogleAccount>?
    {
        val googleAccounts = accountRepository.loadAccounts(context)
        if (googleAccounts.isEmpty()) { return null }

        return accountRepository.loadAccounts(context)
    }

    fun getOutlookAccounts(): List<OutlookAccount> {
        return emptyList()
    }
}