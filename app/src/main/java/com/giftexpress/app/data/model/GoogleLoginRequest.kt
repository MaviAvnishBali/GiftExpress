package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class for Google Login API request
 */
data class GoogleLoginRequest(
//    @SerializedName("idToken")
//    val idToken: String?,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("firstname")
    val firstname: String,
    
    @SerializedName("lastname")
    val lastname: String,

    @SerializedName("socialId")
    val socialId: String,

    @SerializedName("type")
    val type: String
)
