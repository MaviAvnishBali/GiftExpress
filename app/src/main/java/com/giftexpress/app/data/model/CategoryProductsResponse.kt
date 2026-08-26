package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement

data class CategoryProductsResponse(
    @SerializedName("status_code") val statusCode: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("items") val items: List<SliderProduct>? = null,
    @SerializedName("total_count") val totalCount: Int? = null,
    @SerializedName("page_size") val pageSize: Int? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("total_pages") val totalPages: Int? = null,
    @SerializedName("filters") val filters: List<ProductFilter>? = null
)

data class ProductFilter(
    @SerializedName("name") val name: String? = null,
    @SerializedName("request_var") val requestVar: String? = null,
    @SerializedName("options") val options: List<FilterOption>? = null
)

data class FilterOption(
    @SerializedName("label") val labelRaw: JsonElement? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("count") val count: Int? = null
) {
    val label: String?
        get() = if (labelRaw?.isJsonArray == true) {
            labelRaw.asJsonArray.joinToString(", ") { it.asString }
        } else if (labelRaw?.isJsonPrimitive == true) {
            labelRaw.asString
        } else {
            null
        }
}
