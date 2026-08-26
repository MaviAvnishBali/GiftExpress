package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class OrderApiResponse(
    @SerializedName("entity_id") val entityId: Int? = null,
    @SerializedName("increment_id") val incrementId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("grand_total") val grandTotal: Double? = null,
    @SerializedName("subtotal") val subtotal: Double? = null,
    @SerializedName("shipping_amount") val shippingAmount: Double? = null,
    @SerializedName("tax_amount") val taxAmount: Double? = null,
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("coupon_code") val couponCode: String? = null,
    @SerializedName("total_qty_ordered") val totalQtyOrdered: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("customer_firstname") val customerFirstname: String? = null,
    @SerializedName("customer_lastname") val customerLastname: String? = null,
    @SerializedName("customer_email") val customerEmail: String? = null,
    @SerializedName("items") val items: List<OrderApiItem>? = null,
    @SerializedName("billing_address") val billingAddress: OrderApiAddress? = null,
    @SerializedName("payment") val payment: OrderApiPayment? = null,
    @SerializedName("extension_attributes") val extensionAttributes: OrderApiExtension? = null,
    @SerializedName("shipping_description") val shippingDescription: String? = null
) {
    fun getPaymentTitle(): String {
        extensionAttributes?.paymentAdditionalInfo
            ?.firstOrNull { it.key == "method_title" }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        // Fallback for some payment methods
        payment?.additionalInformation?.find { it.contains("Credit Card", ignoreCase = true) || it.contains("PayPal", ignoreCase = true) }
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        return payment?.method ?: "N/A"
    }

    fun getShippingAddress(): OrderApiAddress? =
        extensionAttributes?.shippingAssignments?.firstOrNull()?.shipping?.address
}

data class OrderApiItem(
    @SerializedName("item_id") val itemId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("qty_ordered") val qtyOrdered: Int? = null,
    @SerializedName("row_total") val rowTotal: Double? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("extension_attributes") val extensionAttributes: OrderItemExtension? = null
)

data class OrderItemExtension(
    @SerializedName("product_image") val productImage: String? = null,
    @SerializedName("available_sizes") val availableSizes: String? = null
)

data class OrderApiAddress(
    @SerializedName("firstname") val firstname: String? = null,
    @SerializedName("lastname") val lastname: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("country_id") val countryId: String? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("telephone") val telephone: String? = null,
    @SerializedName("street") val street: List<String>? = null
) {
    fun fullName() = "${firstname ?: ""} ${lastname ?: ""}".trim()
    fun fullAddress() = buildString {
        street?.joinToString(", ")?.let { append(it) }
        city?.let { append(", $it") }
        region?.let { append(", $it") }
        postcode?.let { append(" $it") }
    }.trim().trimStart(',').trim()
}

data class OrderApiPayment(
    @SerializedName("method") val method: String? = null,
    @SerializedName("amount_paid") val amountPaid: Double? = null,
    @SerializedName("additional_information") val additionalInformation: List<String>? = null
)

data class OrderApiExtension(
    @SerializedName("shipping_assignments") val shippingAssignments: List<ShippingAssignment>? = null,
    @SerializedName("payment_additional_info") val paymentAdditionalInfo: List<PaymentInfoEntry>? = null,
    @SerializedName("amextrafee_fee_amount") val amextrafeeFeeAmount: Double? = null,
    @SerializedName("order_source") val orderSource: String? = null
)

data class ShippingAssignment(
    @SerializedName("shipping") val shipping: ShippingInfo? = null
)

data class ShippingInfo(
    @SerializedName("method") val method: String? = null,
    @SerializedName("address") val address: OrderApiAddress? = null
)

data class PaymentInfoEntry(
    @SerializedName("key") val key: String? = null,
    @SerializedName("value") val value: String? = null
)
