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
    @SerializedName(value = "name", alternate = ["title", "brand_name", "label", "category_name"]) val name: String? = null,
    @SerializedName("price") val price: Double? = null,
    // The all-products / special-products endpoint (Best Sellers, New Arrivals) returns the
    // product image under a different key than the category/home sliders. Accept the common
    // variants so the image is not dropped on those pages. "image" stays the primary key,
    // so the already-working listings are unaffected.
    @SerializedName(
        value = "image",
        alternate = ["image_url", "product_image", "mainImage", "main_image", "thumbnail", "small_image", "icon"]
    ) val image: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("as_low_as") val asLowAs: Boolean? = null,
    @SerializedName("attributes") val attributes: List<String>? = null,
    @SerializedName(value = "perfume_type", alternate = ["perfumeType", "type"]) val perfumeType: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null
) {
    val subtitle: String
        get() = perfumeType ?: "Eau De Parfum"
}

data class SliderBanner(
    @SerializedName("title") val title: String? = null,
    @SerializedName("mob_image") val mobImage: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("description") val description: String? = null,
    // Matches iOS Banner.type and Banner.urlApi — used for tap navigation
    @SerializedName("type") val type: String? = null,     // "product" | "category" | "brand" | ""
    @SerializedName("url_api") val urlApi: String? = null, // sku / categoryId / brandId
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("id") val id: String? = null
)

data class SliderOffer(
    @SerializedName("image") val image: String = "",
    // Matches iOS Offer.type and Offer.url_api — used for tap navigation
    @SerializedName("type") val type: String = "",
    @SerializedName("url_api") val urlApi: String = ""
)
