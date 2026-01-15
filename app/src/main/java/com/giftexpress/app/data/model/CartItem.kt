package com.giftexpress.app.data.model

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val sku: String,
    val size: String,
    var quantity: Int,
    val image: String
)
