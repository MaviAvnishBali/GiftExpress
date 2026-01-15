package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class CategoryProductsResponse(
    @SerializedName("status_code") val statusCode: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("items") val items: List<SliderProduct>? = null,
    @SerializedName("total_count") val totalCount: Int? = null,
    @SerializedName("page_size") val pageSize: Int? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("total_pages") val totalPages: Int? = null
)
