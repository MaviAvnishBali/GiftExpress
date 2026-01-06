package com.giftexpress.app.data.model

/**
 * Data class representing User information
 * Used in profile screen and session management
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val token: String
)
