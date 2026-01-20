package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class SliderResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("sequence") val sequence: Int? = null,
    @SerializedName("products") val products: List<SliderProduct>? = null,
    @SerializedName("banners") val banners: List<SliderBanner>? = null,
    @SerializedName("offers") val offers: List<SliderOffer>? = null,
    @SerializedName("categories") val categories: List<SliderProduct>? = null
)

data class SliderProduct(
    @SerializedName("name") val name: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("attributes") val attributes: List<String>? = null,
    @SerializedName("perfumeType") val perfumeType: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null
)

data class SliderBanner(
    @SerializedName("title") val title: String? = null,
    @SerializedName("mob_image") val mobImage: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("description") val description: String? = null
)

data class SliderOffer(
    @SerializedName("image") val image: String
)
