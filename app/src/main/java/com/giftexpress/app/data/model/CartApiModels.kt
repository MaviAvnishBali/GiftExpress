package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for adding an item to the cart
 */
data class AddCartItemRequest(
    @SerializedName("cartItem") val cartItem: AddCartItem
)

data class AddCartItem(
    @SerializedName("sku") val sku: String,
    @SerializedName("qty") val qty: Int,
    @SerializedName("quote_id") val quoteId: Int
)

/**
 * Request model for updating an item in the cart
 */
data class UpdateCartItemRequest(
    @SerializedName("cartItem") val cartItem: UpdateCartItem
)

data class UpdateCartItem(
    @SerializedName("item_id") val itemId: Int,
    @SerializedName("qty") val qty: Int
)

/**
 * Response model for the Cart
 */
data class CartResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("items") val items: List<CartItemDetail>?,
    @SerializedName("items_count") val itemsCount: Int?,
    @SerializedName("items_qty") val itemsQty: Int?,
    @SerializedName("subtotal") val subtotal: Double?,
    @SerializedName("grand_total") val grandTotal: Double?
)

data class CartItemDetail(
    @SerializedName("item_id") val itemId: Int?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("qty") val qty: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("product_type") val productType: String?,
    @SerializedName("quote_id") val quoteId: String?
)
