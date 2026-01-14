package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class MenuItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("url") val url: String,
    @SerializedName("image") val image: String,
    @SerializedName("category_id") val categoryId: Int? = null
)
