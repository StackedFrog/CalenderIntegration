package com.example.calenderintegration.repository

import javax.inject.Inject

class AuthRepository @Inject constructor() {
    fun isLoggedIn(): Boolean {
        // check if user is logged in
        // should be true if any google account is logged in
        return false
    }

    fun logIn(): String? {
        // make the user log in
        // ask sebi for more info
        return "Id_token_or_whatever"
    }
}