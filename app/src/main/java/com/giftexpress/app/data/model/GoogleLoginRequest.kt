package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for Google Login API request
 */
data class GoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String
)
