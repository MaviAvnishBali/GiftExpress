package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class CustomerDetailsResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstname")
    val firstName: String,
    @SerializedName("lastname")
    val lastName: String,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("store_id")
    val storeId: Int,
    @SerializedName("website_id")
    val websiteId: Int
)
