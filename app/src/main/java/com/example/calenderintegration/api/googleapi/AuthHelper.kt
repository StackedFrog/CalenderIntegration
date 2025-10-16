package com.example.calenderintegration.api.googleapi
import android.accounts.Account
import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

object AuthHelper {
    fun createCredential(context: Context, account: Account, scopes: List<String>): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            context,
            scopes
        ).apply {
            selectedAccount = account
        }
    }
}
