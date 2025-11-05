package com.example.calenderintegration.repository

import android.content.Context
import com.example.calenderintegration.api.googleapi.GoogleAccountRepository
import com.example.calenderintegration.api.zohoapi.ZohoAccountRepository
import com.example.calenderintegration.model.GoogleAccount
import com.example.calenderintegration.model.ZohoAccount

import javax.inject.Inject
import javax.inject.Singleton


/**
 *
 * Pablo 1/11: Added zohoStore reference to the AccountsRepository
 */
@Singleton
class AccountsRepository @Inject constructor(
    private val googleStore: GoogleAccountRepository,
    // private val zohoStore: ZohoAccountRepository
) {


    // ------------------- Google ------------------

    /** Always return a list (never null). */
    fun getGoogleAccounts(context: Context): List<GoogleAccount> =
        googleStore.loadAccounts(context)

    /** Remove a Google account by email and persist. */
    fun deleteGoogleAccount(context: Context, email: String) {
        val current = googleStore.loadAccounts(context).toMutableList()
        val newList = current.filterNot { it.email.equals(email, ignoreCase = true) }
        googleStore.saveAccounts(context, newList)
    }

//    // ------------------ Zoho ----------------
//    fun getZohoAccounts(context: Context): List<ZohoAccount> {
//        return zohoStore.loadAccounts(context)
//    }
//
//    fun deleteZohoAccount(context: Context, email: String) {
//        val current = zohoStore.loadAccounts(context).toMutableList()
//        val newList = current.filterNot { it.email.equals(email, ignoreCase = true) }
//        zohoStore.saveAccounts(context, newList)
//    }
}