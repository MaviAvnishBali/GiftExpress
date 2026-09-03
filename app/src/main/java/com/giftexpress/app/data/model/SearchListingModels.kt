package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class SearchListingApiResponse(
    @SerializedName("status") val status: Int?,
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("requestId") val requestId: String?,
    @SerializedName("responseId") val responseId: String?,
    @SerializedName("payload") val payload: SearchListingPayload?
)

data class SearchListingPayload(
    @SerializedName("result") val result: List<SearchListingProduct>?,
    @SerializedName("total") val total: Int?,
    @SerializedName("pages") val pages: Int?
)

data class SearchListingProduct(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("mainImage") val mainImage: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("finalPrice") val finalPrice: Double?,
    @SerializedName("sellingPrice") val sellingPrice: Double?,
    @SerializedName("sku") val sku: List<String>?,
    @SerializedName("inStock") val inStock: Boolean?,
    @SerializedName("description") val description: String?,
    @SerializedName("discount") val discount: Double?,
    @SerializedName("discountPercentage") val discountPercentage: Double?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("attributes") val attributes: List<SearchListingAttribute>? = null
) {
    val displayPrice: Double get() = sellingPrice ?: finalPrice ?: price ?: 0.0
    val firstSku: String? get() = sku?.firstOrNull()
}

data class SearchListingAttribute(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("values") val values: List<SearchListingAttributeValue>?
)

data class SearchListingAttributeValue(
    @SerializedName("value") val value: List<String>?
)
