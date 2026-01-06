package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for login API request
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)
