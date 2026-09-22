package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class SliderResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("sequence") val sequence: Int? = null,
    @SerializedName("products") val products: List<SliderProduct>? = null,
    @SerializedName("banners") val banners: List<SliderBanner>? = null,
    @SerializedName("offers") val offers: List<SliderOffer>? = null,
    @SerializedName(value = "categories", alternate = ["category_list"]) val categories: List<SliderProduct>? = null
) {
    val displayCategories: List<SliderProduct>
        get() = categories ?: products ?: emptyList()
}

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
    @SerializedName(value = "as_low_as", alternate = ["asLowAs", "is_as_low_as"]) val asLowAs: Boolean? = null,
    @SerializedName("attributes") val attributes: List<String>? = null,
    @SerializedName(value = "perfume_type", alternate = ["perfumeType", "type"]) val perfumeType: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName(value = "product_id", alternate = ["productId", "entity_id"]) val productId: Int? = null,
    @SerializedName(value = "id", alternate = ["brand_id"]) val id: String? = null,
    @SerializedName(value = "category_id", alternate = ["categoryId"]) val categoryId: Int? = null
) {
    val subtitle: String
        get() = perfumeType ?: attributes?.firstOrNull() ?: "Eau De Parfum"

    val brandName: String?
        get() = attributes?.firstOrNull() ?: name

    val resolvedId: Int?
        get() = id?.toIntOrNull() ?: productId ?: categoryId
}

data class SliderBanner(
    @SerializedName("title") val title: String? = null,
    @SerializedName(value = "mob_image", alternate = ["mobImage", "image"]) val mobImage: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("description") val description: String? = null,
    // Matches iOS Banner.type and Banner.urlApi — used for tap navigation
    @SerializedName("type") val type: String? = null,     // "product" | "category" | "brand" | ""
    @SerializedName(value = "url_api", alternate = ["urlApi"]) val urlApi: String? = null, // sku / categoryId / brandId
    @SerializedName(value = "category_id", alternate = ["categoryId"]) val categoryId: Int? = null,
    @SerializedName(value = "product_id", alternate = ["productId"]) val productId: Int? = null,
    @SerializedName("id") val id: String? = null
)

data class SliderOffer(
    @SerializedName(value = "image", alternate = ["mob_image", "mobImage"]) val image: String? = null,
    // Matches iOS Offer.type and Offer.url_api — used for tap navigation
    @SerializedName("type") val type: String? = null,     // "brand" | "category" | "product" | ""
    @SerializedName(value = "url_api", alternate = ["urlApi"]) val urlApi: String? = null,
    @SerializedName(value = "title", alternate = ["name", "label", "brand_name"]) val title: String? = null
)
