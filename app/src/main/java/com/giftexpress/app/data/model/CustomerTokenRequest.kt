package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class CustomerTokenRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)
