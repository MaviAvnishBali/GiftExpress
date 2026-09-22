package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class OrderApiResponse(
    @SerializedName("entity_id") val entityId: Int? = null,
    @SerializedName("increment_id") val incrementId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("grand_total") val grandTotal: Double? = null,
    @SerializedName("base_grand_total") val baseGrandTotal: Double? = null,
    @SerializedName("subtotal") val subtotal: Double? = null,
    @SerializedName("base_subtotal") val baseSubtotal: Double? = null,
    @SerializedName("shipping_amount") val shippingAmount: Double? = null,
    @SerializedName("base_shipping_amount") val baseShippingAmount: Double? = null,
    @SerializedName("tax_amount") val taxAmount: Double? = null,
    @SerializedName("base_tax_amount") val baseTaxAmount: Double? = null,
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("base_discount_amount") val baseDiscountAmount: Double? = null,
    @SerializedName("discount_invoiced") val discountInvoiced: Double? = null,
    @SerializedName("base_discount_invoiced") val baseDiscountInvoiced: Double? = null,
    @SerializedName("discount_tax_compensation_amount") val discountTaxCompensationAmount: Double? = null,
    @SerializedName("base_discount_tax_compensation_amount") val baseDiscountTaxCompensationAmount: Double? = null,
    @SerializedName("shipping_discount_amount") val shippingDiscountAmount: Double? = null,
    @SerializedName("shipping_discount_tax_compensation_amount") val shippingDiscountTaxCompensationAmount: Double? = null,
    @SerializedName("shipping_incl_tax") val shippingInclTax: Double? = null,
    @SerializedName("base_shipping_incl_tax") val baseShippingInclTax: Double? = null,
    @SerializedName("shipping_invoiced") val shippingInvoiced: Double? = null,
    @SerializedName("base_shipping_invoiced") val baseShippingInvoiced: Double? = null,
    @SerializedName("shipping_tax_amount") val shippingTaxAmount: Double? = null,
    @SerializedName("base_shipping_tax_amount") val baseShippingTaxAmount: Double? = null,
    @SerializedName("subtotal_incl_tax") val subtotalInclTax: Double? = null,
    @SerializedName("base_subtotal_incl_tax") val baseSubtotalInclTax: Double? = null,
    @SerializedName("subtotal_invoiced") val subtotalInvoiced: Double? = null,
    @SerializedName("base_subtotal_invoiced") val baseSubtotalInvoiced: Double? = null,
    @SerializedName("tax_invoiced") val taxInvoiced: Double? = null,
    @SerializedName("base_tax_invoiced") val baseTaxInvoiced: Double? = null,
    @SerializedName("total_due") val totalDue: Double? = null,
    @SerializedName("base_total_due") val baseTotalDue: Double? = null,
    @SerializedName("total_invoiced") val totalInvoiced: Double? = null,
    @SerializedName("base_total_invoiced") val baseTotalInvoiced: Double? = null,
    @SerializedName("base_total_invoiced_cost") val baseTotalInvoicedCost: Double? = null,
    @SerializedName("total_paid") val totalPaid: Double? = null,
    @SerializedName("base_total_paid") val baseTotalPaid: Double? = null,
    @SerializedName("total_item_count") val totalItemCount: Int? = null,
    @SerializedName("total_qty_ordered") val totalQtyOrdered: Int? = null,
    @SerializedName("coupon_code") val couponCode: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("customer_id") val customerId: Int? = null,
    @SerializedName("customer_group_id") val customerGroupId: Int? = null,
    @SerializedName("customer_is_guest") val customerIsGuest: Int? = null,
    @SerializedName("customer_firstname") val customerFirstname: String? = null,
    @SerializedName("customer_lastname") val customerLastname: String? = null,
    @SerializedName("customer_email") val customerEmail: String? = null,
    @SerializedName("customer_dob") val customerDob: String? = null,
    @SerializedName("customer_note_notify") val customerNoteNotify: Int? = null,
    @SerializedName("billing_address_id") val billingAddressId: Int? = null,
    @SerializedName("quote_id") val quoteId: Int? = null,
    @SerializedName("quote_address_id") val quoteAddressId: Int? = null,
    @SerializedName("order_currency_code") val orderCurrencyCode: String? = null,
    @SerializedName("base_currency_code") val baseCurrencyCode: String? = null,
    @SerializedName("global_currency_code") val globalCurrencyCode: String? = null,
    @SerializedName("store_currency_code") val storeCurrencyCode: String? = null,
    @SerializedName("store_id") val storeId: Int? = null,
    @SerializedName("store_name") val storeName: String? = null,
    @SerializedName("store_to_base_rate") val storeToBaseRate: Double? = null,
    @SerializedName("store_to_order_rate") val storeToOrderRate: Double? = null,
    @SerializedName("base_to_global_rate") val baseToGlobalRate: Double? = null,
    @SerializedName("base_to_order_rate") val baseToOrderRate: Double? = null,
    @SerializedName("shipping_description") val shippingDescription: String? = null,
    @SerializedName("is_virtual") val isVirtual: Int? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("remote_ip") val remoteIp: String? = null,
    @SerializedName("protect_code") val protectCode: String? = null,
    @SerializedName("items") val items: List<OrderApiItem>? = null,
    @SerializedName("billing_address") val billingAddress: OrderApiAddress? = null,
    @SerializedName("payment") val payment: OrderApiPayment? = null,
    @SerializedName("status_histories") val statusHistories: List<OrderStatusHistory>? = null,
    @SerializedName("extension_attributes") val extensionAttributes: OrderApiExtension? = null,
    @SerializedName("payment_additional_info") val paymentAdditionalInfo: List<PaymentInfoEntry>? = null,
    @SerializedName("tracking_information") private val _trackingInformation: List<TrackingInformation>? = null
) {
    val trackingInformation: List<TrackingInformation>?
        get() = _trackingInformation?.takeIf { it.isNotEmpty() }
            ?: extensionAttributes?.trackingInformation
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
    @SerializedName("order_id") val orderId: Int? = null,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("quote_item_id") val quoteItemId: Int? = null,
    @SerializedName("store_id") val storeId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("product_type") val productType: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("base_price") val basePrice: Double? = null,
    @SerializedName("original_price") val originalPrice: Double? = null,
    @SerializedName("base_original_price") val baseOriginalPrice: Double? = null,
    @SerializedName("price_incl_tax") val priceInclTax: Double? = null,
    @SerializedName("base_price_incl_tax") val basePriceInclTax: Double? = null,
    @SerializedName("qty_ordered") val qtyOrdered: Int? = null,
    @SerializedName("qty_invoiced") val qtyInvoiced: Int? = null,
    @SerializedName("qty_shipped") val qtyShipped: Int? = null,
    @SerializedName("qty_canceled") val qtyCanceled: Int? = null,
    @SerializedName("qty_refunded") val qtyRefunded: Int? = null,
    @SerializedName("row_total") val rowTotal: Double? = null,
    @SerializedName("base_row_total") val baseRowTotal: Double? = null,
    @SerializedName("row_total_incl_tax") val rowTotalInclTax: Double? = null,
    @SerializedName("base_row_total_incl_tax") val baseRowTotalInclTax: Double? = null,
    @SerializedName("row_invoiced") val rowInvoiced: Double? = null,
    @SerializedName("base_row_invoiced") val baseRowInvoiced: Double? = null,
    @SerializedName("row_weight") val rowWeight: Double? = null,
    @SerializedName("tax_amount") val taxAmount: Double? = null,
    @SerializedName("base_tax_amount") val baseTaxAmount: Double? = null,
    @SerializedName("tax_percent") val taxPercent: Double? = null,
    @SerializedName("tax_invoiced") val taxInvoiced: Double? = null,
    @SerializedName("base_tax_invoiced") val baseTaxInvoiced: Double? = null,
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("base_discount_amount") val baseDiscountAmount: Double? = null,
    @SerializedName("discount_percent") val discountPercent: Double? = null,
    @SerializedName("discount_invoiced") val discountInvoiced: Double? = null,
    @SerializedName("base_discount_invoiced") val baseDiscountInvoiced: Double? = null,
    @SerializedName("discount_tax_compensation_amount") val discountTaxCompensationAmount: Double? = null,
    @SerializedName("base_discount_tax_compensation_amount") val baseDiscountTaxCompensationAmount: Double? = null,
    @SerializedName("amount_refunded") val amountRefunded: Double? = null,
    @SerializedName("base_amount_refunded") val baseAmountRefunded: Double? = null,
    @SerializedName("base_cost") val baseCost: Double? = null,
    @SerializedName("free_shipping") val freeShipping: Int? = null,
    @SerializedName("is_qty_decimal") val isQtyDecimal: Int? = null,
    @SerializedName("no_discount") val noDiscount: Int? = null,
    @SerializedName("is_virtual") val isVirtual: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("extension_attributes") val extensionAttributes: OrderItemExtension? = null
)

data class OrderItemExtension(
    @SerializedName("product_image") val productImage: String? = null,
    @SerializedName("available_sizes") val availableSizes: String? = null,
    @SerializedName("itemized_taxes") val itemizedTaxes: List<Any>? = null
)

data class OrderApiAddress(
    @SerializedName("entity_id") val entityId: Int? = null,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("address_type") val addressType: String? = null,
    @SerializedName("firstname") val firstname: String? = null,
    @SerializedName("lastname") val lastname: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("telephone") val telephone: String? = null,
    @SerializedName("street") val street: List<String>? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("region_code") val regionCode: String? = null,
    @SerializedName("region_id") val regionId: Int? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("country_id") val countryId: String? = null
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
    @SerializedName("entity_id") val entityId: Int? = null,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("method") val method: String? = null,
    @SerializedName("last_trans_id") val lastTransId: String? = null,
    @SerializedName("amount_paid") val amountPaid: Double? = null,
    @SerializedName("amount_ordered") val amountOrdered: Double? = null,
    @SerializedName("amount_authorized") val amountAuthorized: Double? = null,
    @SerializedName("base_amount_paid") val baseAmountPaid: Double? = null,
    @SerializedName("base_amount_ordered") val baseAmountOrdered: Double? = null,
    @SerializedName("base_amount_authorized") val baseAmountAuthorized: Double? = null,
    @SerializedName("shipping_amount") val shippingAmount: Double? = null,
    @SerializedName("shipping_captured") val shippingCaptured: Double? = null,
    @SerializedName("base_shipping_amount") val baseShippingAmount: Double? = null,
    @SerializedName("base_shipping_captured") val baseShippingCaptured: Double? = null,
    @SerializedName("additional_information") val additionalInformation: List<String>? = null,
    @SerializedName("cc_last4") val ccLast4: String? = null,
    @SerializedName("cc_type") val ccType: String? = null,
    @SerializedName("cc_exp_year") val ccExpYear: String? = null,
    @SerializedName("cc_ss_start_month") val ccSsStartMonth: String? = null,
    @SerializedName("cc_ss_start_year") val ccSsStartYear: String? = null
)

data class OrderStatusHistory(
    @SerializedName("entity_id") val entityId: Int? = null,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("entity_name") val entityName: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("is_customer_notified") val isCustomerNotified: Int? = null,
    @SerializedName("is_visible_on_front") val isVisibleOnFront: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class OrderApiExtension(
    @SerializedName("shipping_assignments") val shippingAssignments: List<ShippingAssignment>? = null,
    @SerializedName("payment_additional_info") val paymentAdditionalInfo: List<PaymentInfoEntry>? = null,
    @SerializedName("amextrafee_fee_id") val amextrafeeFeeId: String? = null,
    @SerializedName("amextrafee_fee_amount") val amextrafeeFeeAmount: Double? = null,
    @SerializedName("amextrafee_base_fee_amount") val amextrafeeBaseFeeAmount: Double? = null,
    @SerializedName("amextrafee_tax_amount") val amextrafeeTaxAmount: Double? = null,
    @SerializedName("amextrafee_base_tax_amount") val amextrafeeBaseTaxAmount: Double? = null,
    @SerializedName("order_source") val orderSource: String? = null,
    @SerializedName("converting_from_quote") val convertingFromQuote: Boolean? = null,
    @SerializedName("tracking_information") val trackingInformation: List<TrackingInformation>? = null
)

data class ShippingAssignment(
    @SerializedName("shipping") val shipping: ShippingInfo? = null,
    @SerializedName("items") val items: List<OrderApiItem>? = null
)

data class ShippingInfo(
    @SerializedName("method") val method: String? = null,
    @SerializedName("address") val address: OrderApiAddress? = null,
    @SerializedName("total") val total: ShippingTotal? = null
)

data class ShippingTotal(
    @SerializedName("shipping_amount") val shippingAmount: Double? = null,
    @SerializedName("base_shipping_amount") val baseShippingAmount: Double? = null,
    @SerializedName("shipping_incl_tax") val shippingInclTax: Double? = null,
    @SerializedName("base_shipping_incl_tax") val baseShippingInclTax: Double? = null,
    @SerializedName("shipping_tax_amount") val shippingTaxAmount: Double? = null,
    @SerializedName("base_shipping_tax_amount") val baseShippingTaxAmount: Double? = null,
    @SerializedName("shipping_invoiced") val shippingInvoiced: Double? = null,
    @SerializedName("base_shipping_invoiced") val baseShippingInvoiced: Double? = null,
    @SerializedName("shipping_discount_amount") val shippingDiscountAmount: Double? = null,
    @SerializedName("base_shipping_discount_amount") val baseShippingDiscountAmount: Double? = null
)

data class PaymentInfoEntry(
    @SerializedName("key") val key: String? = null,
    @SerializedName("value") val value: String? = null
)

data class TrackingInformation(
    @SerializedName("carrier_code") val carrierCode: String? = null,
    @SerializedName("carrier_title") val carrierTitle: String? = null,
    @SerializedName("tracking_number") val trackingNumber: String? = null,
    @SerializedName("tracking_url") val trackingUrl: String? = null
)
