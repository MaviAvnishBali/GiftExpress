package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for signup API request
 */
data class SignupRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("dateOfBirth")
    val dob: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)
