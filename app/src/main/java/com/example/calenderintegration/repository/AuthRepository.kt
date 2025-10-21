package com.example.calenderintegration.repository

import javax.inject.Inject

class AuthRepository @Inject constructor() {
    fun isLoggedIn(): Boolean {
        return false
    }

    fun logIn(): String? {
        return "Id_token_or_whatever"
    }
}