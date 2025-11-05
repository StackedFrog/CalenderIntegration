package com.example.calenderintegration.model


/**
 * Pablo 1/11: This will hold the result of the token exchange needed for logging in
 */
data class ZohoToken(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)