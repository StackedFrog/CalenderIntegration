package com.example.calenderintegration.repository


import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.OutlookAccount


import android.content.Context


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsRepository @Inject constructor(
    private val googleAccountRepository: GoogleAccountRepository
) {
    /**
     * Retrieves locally saved Google accounts.
     * Returns null if no accounts are found.
     */
    fun getGoogleAccounts(context: Context): List<GoogleAccount>? {
        val googleAccounts = googleAccountRepository.loadAccounts(context)
        return if (googleAccounts.isEmpty()) null else googleAccounts
    }

    /**
     * Placeholder for Outlook accounts — not implemented yet.
     */
    fun getOutlookAccounts(): List<OutlookAccount> = emptyList()
}
