package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class PaymentIntentResponse(
    @SerializedName("client_secret") val clientSecret: String?,
    @SerializedName("payment_intent_id") val paymentIntentId: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?
)

data class StripePaymentIntentRequest(
    @SerializedName("quoteId") val quoteId: Int,
    @SerializedName("customerId") val customerId: Int
)

data class StripePlaceOrderRequest(
    @SerializedName("payment_intent_id") val paymentIntentId: String,
    @SerializedName("payment_method") val paymentMethod: String? = null
)

data class PlaceOrderResponse(
    @SerializedName("payment_intent_id") val paymentIntentId: String? = null,
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("order_id") val orderId: String? = null,
    @SerializedName("increment_id") val incrementId: String? = null,
    @SerializedName("order_increment_id") val orderIncrementId: String? = null,
    @SerializedName("real_order_id") val realOrderId: String? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("order_number") val orderNumber: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("transaction_id") val transactionId: String? = null
) {
    fun getDisplayOrderId(): String {
        val candidates = listOfNotNull(
            incrementId,
            orderIncrementId,
            orderId,
            realOrderId,
            orderNumber,
            entityId,
            id,
            transactionId,
            paymentIntentId
        )
        return candidates.firstOrNull { it.isNotBlank() && it != "null" } ?: "N/A"
    }
}

// ─── PayPal ──────────────────────────────────────────────────────────────────

data class PayPalAutomaticPaymentMethods(
    @SerializedName("enabled") val enabled: Boolean = true
)

data class PayPalCreateOrderRequest(
    @SerializedName("quoteId") val quoteId: Int,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "USD",
    @SerializedName("automatic_payment_methods") val automaticPaymentMethods: PayPalAutomaticPaymentMethods = PayPalAutomaticPaymentMethods(true)
)

data class PayPalCreateOrderResponse(
    @SerializedName("paypal_order_id") val paypalOrderId: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?
)

data class PayPalPlaceOrderRequest(
    @SerializedName("quoteId") val quoteId: Int,
    @SerializedName("orderId") val orderId: String
)

// ─── Web-redirect wallets (Amazon Pay, Afterpay) ───────────────────────────────
// Both are hosted-page checkouts, structurally identical to the PayPal flow:
//   create-checkout → backend opens a provider checkout session for the quote,
//                     returns a redirect_url (hosted approval page) + reference_id
//   place-order     → backend completes the approved session and places the Magento order
// The two providers share these request/response shapes; only the endpoint differs.

data class WebCheckoutSessionRequest(
    @SerializedName("quoteId") val quoteId: Int,
    @SerializedName("customerId") val customerId: Int,
    @SerializedName("test_mode") val testMode: Boolean = true
)

data class WebCheckoutSessionResponse(
    @SerializedName("redirect_url") val redirectUrl: String?,
    @SerializedName("reference_id") val referenceId: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("message") val message: String?
)

data class WebCheckoutPlaceOrderRequest(
    @SerializedName("reference_id") val referenceId: String,
    @SerializedName("payment_method") val paymentMethod: String? = null
)

data class PlaceOrderRequest(
    @SerializedName("paymentMethod") val paymentMethod: PlaceOrderPaymentMethod
)

data class PlaceOrderPaymentMethod(
    @SerializedName("method") val method: String,
    @SerializedName("additional_data") val additionalData: Map<String, String>? = null
)
