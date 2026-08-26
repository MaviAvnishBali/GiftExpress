package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class WishListResponse(
    @SerializedName("items_count") val itemsCount: Int,
    @SerializedName("items") val items: List<WishListItem>
)

data class WishListItem(
    @SerializedName("id") val id: Int,
    @SerializedName("product") val product: WishListProduct,
    @SerializedName("qty") val qty: Int = 1
)

data class WishListProduct(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("sku") val sku: String,
    @SerializedName("name") val name: String,
    @SerializedName("base_image") val baseImage: String,
    @SerializedName("price") val price: Double
)
