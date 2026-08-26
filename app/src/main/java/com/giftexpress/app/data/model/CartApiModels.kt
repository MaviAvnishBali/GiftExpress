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
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("is_virtual") val isVirtual: Boolean?,
    @SerializedName("items") val items: List<CartItemDetail>?,
    @SerializedName("items_count") val itemsCount: Int?,
    @SerializedName("items_qty") val itemsQty: Int?,
    @SerializedName("customer") val customer: CartCustomer?,
    @SerializedName("billing_address") val billingAddress: CartAddress?,
    @SerializedName("orig_order_id") val origOrderId: Int?,
    @SerializedName("currency") val currency: CartCurrency?,
    @SerializedName("customer_is_guest") val customerIsGuest: Boolean?,
    @SerializedName("customer_note_notify") val customerNoteNotify: Boolean?,
    @SerializedName("customer_tax_class_id") val customerTaxClassId: Int?,
    @SerializedName("store_id") val storeId: Int?,
    @SerializedName("extension_attributes") val extensionAttributes: CartExtensionAttributes?,
    @SerializedName("subtotal") val subtotal: Double?,
    @SerializedName("grand_total") val grandTotal: Double?
)

// Matches iOS CartProduct — response from GET carts/mine/items
data class CartItemDetail(
    @SerializedName("item_id") val itemId: Int?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("qty") val qty: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("product_type") val productType: String?,
    @SerializedName("quote_id") val quoteId: String?,
    @SerializedName("extension_attributes") val extensionAttributes: CartItemExtensionAttributes?
) {
    val image: String? get() = extensionAttributes?.productImage
    val size: String? get() = extensionAttributes?.availableSizes
}

// Matches iOS ExtensionAttributes{product_image, available_sizes}
data class CartItemExtensionAttributes(
    @SerializedName("product_image") val productImage: String?,
    @SerializedName("available_sizes") val availableSizes: String?
)

data class CartCustomer(
    @SerializedName("id") val id: Int?,
    @SerializedName("group_id") val groupId: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("created_in") val createdIn: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("firstname") val firstName: String?,
    @SerializedName("lastname") val lastName: String?,
    @SerializedName("store_id") val storeId: Int?,
    @SerializedName("website_id") val websiteId: Int?,
    @SerializedName("addresses") val addresses: List<CartAddress>?,
    @SerializedName("disable_auto_group_change") val disableAutoGroupChange: Int?,
    @SerializedName("extension_attributes") val extensionAttributes: CartCustomerExtensionAttributes?,
    @SerializedName("custom_attributes") val customAttributes: List<CartCustomAttribute>?
)

data class CartCustomerExtensionAttributes(
    @SerializedName("is_subscribed") val isSubscribed: Boolean?
)

data class CartCustomAttribute(
    @SerializedName("attribute_code") val attributeCode: String?,
    @SerializedName("value") val value: String?
)

data class CartAddress(
    @SerializedName("id") val id: Int?,
    @SerializedName("region") val region: String?,
    @SerializedName("region_id") val regionId: Int?,
    @SerializedName("region_code") val regionCode: String?,
    @SerializedName("country_id") val countryId: String?,
    @SerializedName("street") val street: List<String>?,
    @SerializedName("telephone") val telephone: String?,
    @SerializedName("postcode") val postcode: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("firstname") val firstName: String?,
    @SerializedName("lastname") val lastName: String?,
    @SerializedName("customer_id") val customerId: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("same_as_billing") val sameAsBilling: Int?,
    @SerializedName("save_in_address_book") val saveInAddressBook: Int?
)

data class CartCurrency(
    @SerializedName("global_currency_code") val globalCurrencyCode: String?,
    @SerializedName("base_currency_code") val baseCurrencyCode: String?,
    @SerializedName("store_currency_code") val storeCurrencyCode: String?,
    @SerializedName("quote_currency_code") val quoteCurrencyCode: String?,
    @SerializedName("store_to_base_rate") val storeToBaseRate: Double?,
    @SerializedName("store_to_quote_rate") val storeToQuoteRate: Double?,
    @SerializedName("base_to_global_rate") val baseToGlobalRate: Double?,
    @SerializedName("base_to_quote_rate") val baseToQuoteRate: Double?
)

data class CartExtensionAttributes(
    @SerializedName("shipping_assignments") val shippingAssignments: List<CartShippingAssignment>?
)

data class CartShippingAssignment(
    @SerializedName("shipping") val shipping: CartShipping?,
    @SerializedName("items") val items: List<CartItemDetail>?
)

data class CartShipping(
    @SerializedName("address") val address: CartAddress?,
    @SerializedName("method") val method: String?
)
