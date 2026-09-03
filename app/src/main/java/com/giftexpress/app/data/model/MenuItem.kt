package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null
)
