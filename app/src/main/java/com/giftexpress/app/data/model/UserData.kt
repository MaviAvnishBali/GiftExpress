package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * User data from API response
 */
data class UserData(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    
    @SerializedName("token")
    val token: String
)
