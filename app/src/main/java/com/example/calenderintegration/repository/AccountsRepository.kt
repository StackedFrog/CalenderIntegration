package com.example.calenderintegration.repository

import android.content.Context
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.model.GoogleAccount

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsRepository @Inject constructor(
    private val googleStore: GoogleAccountRepository
) {
    /** Always return a list (never null). */
    fun getGoogleAccounts(context: Context): List<GoogleAccount> =
        googleStore.loadAccounts(context)

    /** Remove a Google account by email and persist. */
    fun deleteGoogleAccount(context: Context, email: String) {
        val current = googleStore.loadAccounts(context).toMutableList()
        val newList = current.filterNot { it.email.equals(email, ignoreCase = true) }
        googleStore.saveAccounts(context, newList)
    }
}