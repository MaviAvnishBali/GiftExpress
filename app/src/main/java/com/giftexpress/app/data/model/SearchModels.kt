package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

// Trending
data class TrendingApiResponse(
    @SerializedName("status") val status: Int?,
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("requestId") val requestId: String?,
    @SerializedName("responseId") val responseId: String?,
    @SerializedName("payload") val payload: TrendingPayload?
)

data class TrendingPayload(
    @SerializedName("queries") val queries: List<String>?
)

// Autocomplete / Search
data class SearchApiResponse(
    @SerializedName("status") val status: Int?,
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("requestId") val requestId: String?,
    @SerializedName("responseId") val responseId: String?,
    @SerializedName("payload") val payload: SearchPayload?
)

data class SearchPayload(
    @SerializedName("categories") val categories: List<SearchCategory>?,
    @SerializedName("brands") val brands: List<SearchCategory>?,
    @SerializedName("others") val others: List<SearchCategory>?,
    @SerializedName("products") val products: SearchProductContainer?
)

data class SearchProductContainer(
    @SerializedName("result") val result: List<SearchProductItem>?,
    @SerializedName("total") val total: Int?,
    @SerializedName("pages") val pages: Int?,
    @SerializedName("hasExactMatch") val hasExactMatch: Boolean?
)

data class SearchCategory(
    @SerializedName("value") val value: String?,
    @SerializedName("valueHighlighted") val valueHighlighted: String?
)

data class SearchProductItem(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("mainImage") val mainImage: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("finalPrice") val finalPrice: Double?,
    @SerializedName("sellingPrice") val sellingPrice: Double?,
    @SerializedName("sku") val sku: List<String>?,
    @SerializedName("inStock") val inStock: Boolean?
)
