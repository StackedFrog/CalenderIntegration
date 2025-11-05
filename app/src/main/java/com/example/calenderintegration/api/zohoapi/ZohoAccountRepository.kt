package com.example.calenderintegration.api.zohoapi

import android.content.Context
import com.example.calenderintegration.model.ZohoAccount
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 * Pablo 1/11 : Made this placeholder for the ZohoAccountRepository class
 *              maybe check Sebastian's 'GoogleAccountRepository' for example
 */
@Singleton
class ZohoAccountRepository @Inject constructor() {

    private val PREFS_NAME = "zoho_accounts_prefs"
    private val KEY_ACCOUNTS = "accounts"

    fun loadAccounts(context: Context): List<ZohoAccount> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()

        val jsonArray = JSONArray(jsonString)
        val accounts = mutableListOf<ZohoAccount>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            accounts.add(
                ZohoAccount(
                    email = obj.getString("email"),
                    accessToken = obj.getString("accessToken"),
                    refreshToken = obj.optString("refreshToken", null),
                    expiresIn = obj.getLong("expiresIn")
                )
            )
        }
        return accounts
    }

    fun saveAccounts(context: Context, accounts: List<ZohoAccount>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        accounts.forEach { account ->
            val obj = JSONObject()
            obj.put("email", account.email)
            obj.put("accessToken", account.accessToken)
            obj.put("refreshToken", account.refreshToken)
            obj.put("expiresIn", account.expiresIn)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_ACCOUNTS, jsonArray.toString()).apply()
    }

    fun addAccount(context: Context, account: ZohoAccount) {
        val accounts = loadAccounts(context).toMutableList()
        // Replace if email already exists
        accounts.removeAll { it.email == account.email }
        accounts.add(account)
        saveAccounts(context, accounts)
    }
}