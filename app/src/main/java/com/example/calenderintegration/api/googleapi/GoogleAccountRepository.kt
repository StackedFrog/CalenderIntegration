package com.example.calenderintegration.api.googleapi

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.calenderintegration.model.GoogleAccount
import org.json.JSONArray
import org.json.JSONObject

object GoogleAccountRepository {

    private const val PREF_NAME = "google_accounts"

    fun saveAccounts(context: Context, accounts: List<GoogleAccount>) { // dont care about this
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val json = JSONArray()
        accounts.forEach { acc ->
            val obj = JSONObject().apply {
                put("email", acc.email)
                put("displayName", acc.displayName)
                put("accessToken", acc.accessToken)
                put("idToken", acc.idToken)
            }
            json.put(obj)
        }

        prefs.edit().putString("accounts", json.toString()).apply()
    }

    fun loadAccounts(context: Context): List<GoogleAccount> { // this as well needs to be used

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val data = prefs.getString("accounts", null) ?: return emptyList()
        val json = JSONArray(data)

        val accounts = mutableListOf<GoogleAccount>()
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            accounts.add(
                GoogleAccount(
                    email = obj.optString("email", ""),
                    displayName = obj.optString("displayName", ""),
                    accessToken = obj.optString("accessToken", ""),
                    idToken = obj.optString("idToken")
                )
            )
        }
        return accounts
    }

    fun addAccount(context: Context, account: GoogleAccount) { // USE THIIIIIIIIIIIIIIIIIIIIIIIIISSSSSSSSSSSSSSSSS
        val current = loadAccounts(context).toMutableList()
        if (current.none { it.email == account.email })
            current.add(account)
        saveAccounts(context, current)
    }
}