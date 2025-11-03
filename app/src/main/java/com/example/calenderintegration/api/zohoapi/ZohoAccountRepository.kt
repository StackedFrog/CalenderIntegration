package com.example.calenderintegration.api.zohoapi

import android.content.Context
import com.example.calenderintegration.model.ZohoAccount
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 * Pablo 1/11 : Made this placeholder for the ZohoAccountRepository class
 *              maybe check Sebastian's 'GoogleAccountRepository' for example
 */
@Singleton
class ZohoAccountRepository @Inject constructor() {

    /** Load saved Zoho accounts from storage */
    fun loadAccounts(context: Context): List<ZohoAccount> {
        // Implementation depends on SharedPreferences, Room, or other storage
        // Placeholder example
        return emptyList()
    }

    /** Save Zoho accounts to storage */
    fun saveAccounts(context: Context, accounts: List<ZohoAccount>) {
        // Implementation depends on SharedPreferences, Room, or other storage
    }

    /** Add a new Zoho account to storage */
    fun addAccount(context: Context, account: ZohoAccount)
    {
        //
    }
}