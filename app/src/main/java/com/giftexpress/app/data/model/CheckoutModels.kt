package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class ShippingMethod(
    @SerializedName("carrier_code") val carrierCode: String?,
    @SerializedName("method_code") val methodCode: String?,
    @SerializedName("carrier_title") val carrierTitle: String?,
    @SerializedName("method_title") val methodTitle: String?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("base_amount") val baseAmount: Double?,
    @SerializedName("available") val available: Boolean?,
    @SerializedName("error_message") val errorMessage: String?,
    @SerializedName("price_excl_tax") val priceExclTax: Double?,
    @SerializedName("price_incl_tax") val priceInclTax: Double?
) {
    val displayTitle: String get() = "${carrierTitle ?: ""} - ${methodTitle ?: ""}".trim(' ', '-')
    val displayAmount: Double get() = amount ?: 0.0
}

data class EstimateShippingRequest(
    @SerializedName("address") val address: ShippingEstimateAddress
)

data class ShippingEstimateAddress(
    @SerializedName("region") val region: String,
    @SerializedName("region_id") val regionId: Int,
    @SerializedName("region_code") val regionCode: String,
    @SerializedName("country_id") val countryId: String,
    @SerializedName("street") val street: List<String>,
    @SerializedName("postcode") val postcode: String,
    @SerializedName("city") val city: String,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("telephone") val telephone: String
)

data class ShippingInformationRequest(
    @SerializedName("addressInformation") val addressInformation: AddressInformation
)

data class AddressInformation(
    @SerializedName("shipping_address") val shippingAddress: ShippingEstimateAddress,
    @SerializedName("billing_address") val billingAddress: ShippingEstimateAddress,
    @SerializedName("shipping_carrier_code") val shippingCarrierCode: String,
    @SerializedName("shipping_method_code") val shippingMethodCode: String
)

data class PaymentMethodsResponse(
    @SerializedName("payment_methods") val paymentMethods: List<PaymentMethod>?,
    @SerializedName("totals") val totals: OrderTotals?
)

data class PaymentMethod(
    @SerializedName("code") val code: String?,
    @SerializedName("title") val title: String?
)

data class OrderTotals(
    @SerializedName("grand_total") val grandTotal: Double?,
    @SerializedName("base_grand_total") val baseGrandTotal: Double?,
    @SerializedName("subtotal") val subtotal: Double?,
    @SerializedName("base_subtotal") val baseSubtotal: Double?,
    @SerializedName("discount_amount") val discountAmount: Double?,
    @SerializedName("base_discount_amount") val baseDiscountAmount: Double?,
    @SerializedName("subtotal_with_discount") val subtotalWithDiscount: Double?,
    @SerializedName("shipping_amount") val shippingAmount: Double?,
    @SerializedName("tax_amount") val taxAmount: Double?,
    @SerializedName("items_qty") val itemsQty: Int?,
    @SerializedName("total_segments") val totalSegments: List<TotalSegment>?,
    @SerializedName("extension_attributes") val extensionAttributes: TotalsExtension?
)

data class TotalSegment(
    @SerializedName("code") val code: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("value") val value: Double?,
    @SerializedName("extension_attributes") val extensionAttributes: SegmentExtension?
)

data class SegmentExtension(
    @SerializedName("tax_amasty_extrafee_details") val taxAmastyExtrafeeDetails: TaxAmastyExtrafeeDetails?
)

data class TaxAmastyExtrafeeDetails(
    @SerializedName("value_incl_tax") val valueInclTax: Double?
)

data class TotalsExtension(
    @SerializedName("amasty_rewards_highlight") val rewardsHighlight: RewardsHighlight?
)

data class RewardsHighlight(
    @SerializedName("visible") val visible: Boolean?,
    @SerializedName("caption_text") val captionText: String?
)
