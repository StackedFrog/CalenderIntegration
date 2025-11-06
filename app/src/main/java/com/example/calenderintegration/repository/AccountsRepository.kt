package com.example.calenderintegration.repository

import android.content.Context
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.ZohoAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsRepository @Inject constructor(
    private val googleStore: GoogleAccountRepository,
    private val zohoStore: ZohoAccountRepository = ZohoAccountRepository() // minimal default
) {

    // ---------- Google ----------
    fun getGoogleAccounts(context: Context): List<GoogleAccount> =
        googleStore.loadAccounts(context)

    fun deleteGoogleAccount(context: Context, email: String) {
        val current = googleStore.loadAccounts(context).toMutableList()
        val newList = current.filterNot { it.email.equals(email, ignoreCase = true) }
        googleStore.saveAccounts(context, newList)
    }

    // ---------- Zoho (simple email-only storage) ----------
    fun getZohoAccounts(context: Context): List<ZohoAccount> =
        zohoStore.loadAccounts(context)

    fun addZohoAccount(context: Context, email: String) {
        // store a minimal account; tokens empty for now
        val account = ZohoAccount(
            email = email,
            accessToken = "",
            refreshToken = null,
            expiresIn = 0L
        )
        zohoStore.addAccount(context, account)
    }

    fun deleteZohoAccount(context: Context, email: String) {
        val current = zohoStore.loadAccounts(context).toMutableList()
        val newList = current.filterNot { it.email.equals(email, ignoreCase = true) }
        zohoStore.saveAccounts(context, newList)
    }
}
