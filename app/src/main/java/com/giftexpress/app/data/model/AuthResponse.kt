package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for authentication API response
 * Represents response from login/signup endpoints
 */
data class AuthResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("body")
    val body: String? = null,
    
    @SerializedName("userId")
    val userId: Int? = null,
    
    // In real app, these would be actual fields
    val token: String? = null,
    val message: String? = null
)
