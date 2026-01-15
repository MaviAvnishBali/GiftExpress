package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductDetailsResponse(
    @SerializedName("ProductDeatils") val productDetails: List<ProductDetail>?,
    @SerializedName("MoreProductList") val moreProductList: List<MoreProduct>?
)

data class ProductDetail(
    @SerializedName("product_id") val productId: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("discount_price") val discountPrice: Double?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("attributes") val attributes: ProductAttributes?,
    @SerializedName("size") val size: String?,
    @SerializedName("details") val details: ProductDescriptionDetails?,
    @SerializedName("shippingInformation") val shippingInformation: ShippingInformation?,
    @SerializedName("images") val images: List<String>?
)

data class ProductAttributes(
    @SerializedName("sku") val sku: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("Gender") val gender: String?,
    @SerializedName("upc") val upc: String?,
    @SerializedName("Tester") val tester: String?,
    @SerializedName("free_shipping_text") val freeShippingText: String?
)

data class ProductDescriptionDetails(
    @SerializedName("description") val description: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("perfume_type") val perfumeType: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("Gender") val gender: String?,
    @SerializedName("upc") val upc: String?,
    @SerializedName("manufacturer") val manufacturer: String?
)

data class ShippingInformation(
    @SerializedName("details") val details: String?
)

data class MoreProduct(
    @SerializedName("product_id") val productId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("image") val image: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("attributes") val attributes: List<String>?
)
