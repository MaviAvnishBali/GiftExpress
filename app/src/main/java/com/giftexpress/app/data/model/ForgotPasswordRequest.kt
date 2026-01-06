package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("template")
    val template: String = "email_reset",
    @SerializedName("websiteId")
    val websiteId: Int = 1
)
