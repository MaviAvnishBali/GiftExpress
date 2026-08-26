package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class CmsPage(
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null
)

data class EnquiryRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("perfume_name") val perfumeName: String,
    @SerializedName("reference_url") val referenceUrl: String,
    @SerializedName("message") val message: String
)
