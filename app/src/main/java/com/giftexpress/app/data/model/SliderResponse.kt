package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class SliderResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("products") val products: List<SliderProduct>? = null,
    @SerializedName("banners") val banners: List<SliderBanner>? = null,
    @SerializedName("offers") val offers: List<SliderOffer>? = null
)

data class SliderProduct(
    @SerializedName("name") val name: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("image") val image: String,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("attributes") val attributes: List<String>? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("id") val id: String? = null
)

data class SliderBanner(
    @SerializedName("title") val title: String,
    @SerializedName("mob_image") val mobImage: String,
    @SerializedName("url") val url: String,
    @SerializedName("position") val position: String,
    @SerializedName("description") val description: String?
)

data class SliderOffer(
    @SerializedName("image") val image: String
)
