package com.example.calenderintegration.api.googleapi

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.calenderintegration.model.GoogleAccount
import org.json.JSONArray
import org.json.JSONObject

object GoogleAccountRepository {

    private const val PREF_NAME = "google_accounts"

    // saves an account with an encryption, has to be parsed in to Json due to the ability of grouping of one account data into one line, one object
    fun saveAccounts(context: Context, accounts: List<GoogleAccount>) {
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
                //put("idToken", acc.idToken) //might not be needed anymore, thought it was initially needed
            }
            json.put(obj)
        }

        prefs.edit().putString("accounts", json.toString()).apply()
    }


    // function loads all previously saved Google accounts from encrypted local storage on the device
    fun loadAccounts(context: Context): List<GoogleAccount> {
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
            val acc = GoogleAccount(
                email = obj.optString("email", ""),
                displayName = obj.optString("displayName", ""),
                accessToken = obj.optString("accessToken", ""),
                //idToken = obj.optString("idToken", "") // might not be needed anymore
            )
            Log.d("GoogleAccountRepository", "Loaded account: $acc")
            accounts.add(acc)
        }

        Log.d("GoogleAccountRepository", "Final list of accounts: $accounts")
        return accounts
    }


    // saves account using the top function using an encryption
    fun addAccount(context: Context, account: GoogleAccount) {
        val current = loadAccounts(context).toMutableList()
        if (current.none { it.email == account.email })
            current.add(account)
        saveAccounts(context, current)
    }
}