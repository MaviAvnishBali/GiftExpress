package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class SliderResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("products") val products: List<SliderProduct>? = null,
    @SerializedName("banners") val banners: List<SliderBanner>? = null
)

data class SliderProduct(
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("image") val image: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("attributes") val attributes: List<String>
)

data class SliderBanner(
    @SerializedName("title") val title: String,
    @SerializedName("mob_image") val mobImage: String,
    @SerializedName("url") val url: String,
    @SerializedName("position") val position: String,
    @SerializedName("description") val description: String?
)
