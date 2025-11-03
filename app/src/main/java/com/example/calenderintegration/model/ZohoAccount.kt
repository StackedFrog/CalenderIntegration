package com.example.calenderintegration.model


/**
 *
 * Pablo 1/11: Added ZohoAccount class to represent the Zoho account since there was none before
 */
data class ZohoAccount (
    val email: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long
)