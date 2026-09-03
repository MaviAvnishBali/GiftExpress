package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for Brand List API
 * GET: /rest/V1/giftexpress/brands
 */
data class BrandResponse(
    @SerializedName("brand_id")
    val id: Int? = null,

    @SerializedName("label")
    val name: String? = null,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("product_count")
    val productCount: Int? = null,

    @SerializedName("description")
    val description: String? = null
)


/**
 * Response model for Brand Listing API (Products by Brand)
 * GET: /rest/V1/giftexpress/brand/{brandId}/products
 */
data class ProductListResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("items") val items: List<ProductItem>,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("filters") val filters: List<ProductFilter>?
)

data class ProductItem(
    @SerializedName("product_id") val productId: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("image") val image: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("attributes") val attributes: List<String>?,
    @SerializedName("perfume_type") val perfumeType: String?,
    @SerializedName("as_low_as") val asLowAs: Boolean?
)
