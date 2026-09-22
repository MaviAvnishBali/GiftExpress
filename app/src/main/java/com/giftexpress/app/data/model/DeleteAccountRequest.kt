package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class DeleteAccountRequest(
    @SerializedName("password")
    val password: String? = null
)
