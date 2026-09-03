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
    @SerializedName("payment_additional_info") val paymentAdditionalInfo: List<PaymentInfoEntry>? = null,
    @SerializedName("shipping_description") val shippingDescription: String? = null
) {
    fun getPaymentTitle(): String {
        // Match iOS logic
        // 1. Check root payment_additional_info for method_title
        paymentAdditionalInfo
            ?.firstOrNull { it.key == "method_title" }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
            
        // 2. Check extension_attributes as fallback (in case API returns it there)
        extensionAttributes?.paymentAdditionalInfo
            ?.firstOrNull { it.key == "method_title" }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        // 3. Fallback to array-based data in payment?.additionalInformation
        payment?.additionalInformation?.let { arr ->
            // In Stripe orders, arr[2] is sometimes "1" (mobile_api_order flag). 
            // The title is usually at index 5 or index 2 depending on the method.
            val titleCandidate = arr.find { it.contains("card", ignoreCase = true) || it.contains("paypal", ignoreCase = true) || it.contains("amazon", ignoreCase = true) || it.contains("klarna", ignoreCase = true) || it.contains("afterpay", ignoreCase = true) }
            if (!titleCandidate.isNullOrBlank()) {
                return titleCandidate
            }
            
            if (arr.size > 2 && arr[2].isNotBlank() && arr[2] != "1" && arr[2] != "0") {
                return arr[2]
            }
        }

        // 4. Final fallback
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
    @SerializedName("additional_information") val additionalInformation: List<String>? = null,
    @SerializedName("cc_last4") val ccLast4: String? = null,
    @SerializedName("cc_type") val ccType: String? = null
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
