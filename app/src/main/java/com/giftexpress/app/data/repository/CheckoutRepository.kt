package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.*
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject



class CheckoutRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun estimateShippingMethods(request: EstimateShippingRequest): NetworkResult<List<ShippingMethod>> {
        return try {
            val response = apiService.estimateShippingMethods(request)
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun saveShippingInformation(request: ShippingInformationRequest): NetworkResult<PaymentMethodsResponse> {
        return try {
            val response = apiService.saveShippingInformation(request)
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun applyCoupon(couponCode: String): NetworkResult<Boolean> {
        return try {
            val response = apiService.applyCoupon(couponCode)
            if (response.isSuccessful) NetworkResult.Success(response.body() ?: true)
            else NetworkResult.Error("Invalid coupon: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun removeCoupon(): NetworkResult<Boolean> {
        return try {
            val response = apiService.removeCoupon()
            if (response.isSuccessful) NetworkResult.Success(response.body() ?: true)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun applyRewardPoints(points: Int): NetworkResult<Boolean> {
        return try {
            val response = apiService.applyRewardPoints(points)
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun removeRewardPoints(): NetworkResult<Boolean> {
        return try {
            val response = apiService.removeRewardPoints()
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun placeOrder(request: PlaceOrderRequest): NetworkResult<PlaceOrderResponse> {
        return try {
            val response = apiService.placeOrder(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Order failed")
                } else {
                    NetworkResult.Success(body)
                }
            } else NetworkResult.Error("Order failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Matches iOS CartManager.getCartCount: GET carts/mine/totals
    suspend fun getCartTotals(): NetworkResult<OrderTotals> {
        return try {
            val response = apiService.getCartTotals()
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to get totals: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun applyShippingProtection(request: Map<String, Any>): NetworkResult<OrderTotals> {
        return try {
            val response = apiService.applyShippingProtection(request)
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to apply shipping protection: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Stripe: matches iOS PaymentViewModel.getPaymentIntent
    suspend fun getStripePaymentIntent(quoteId: Int, customerId: Int): NetworkResult<PaymentIntentResponse> {
        return try {
            val response = apiService.getStripePaymentIntent(StripePaymentIntentRequest(quoteId, customerId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Failed to get payment intent")
                } else {
                    NetworkResult.Success(body)
                }
            } else NetworkResult.Error("Failed to get payment intent: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Stripe: matches iOS PaymentViewModel.getOrderDetails (place-order after payment)
    suspend fun stripePlaceOrder(paymentIntentId: String): NetworkResult<PlaceOrderResponse> {
        return try {
            val response = apiService.stripePlaceOrder(StripePlaceOrderRequest(paymentIntentId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Order confirmation failed")
                } else {
                    NetworkResult.Success(body)
                }
            } else NetworkResult.Error("Order confirmation failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // PayPal: create a PayPal order for the current quote (server-side, Orders v2 API)
    suspend fun createPayPalOrder(quoteId: Int, amount: String): NetworkResult<PayPalCreateOrderResponse> {
        return try {
            val response = apiService.createPayPalOrder(
                PayPalCreateOrderRequest(
                    quoteId = quoteId,
                    amount = amount
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Could not create PayPal order")
                } else {
                    NetworkResult.Success(body)
                }
            } else if (response.code() == 404) NetworkResult.Error("PayPal is not available yet. Please use another payment method.")
            else NetworkResult.Error("Could not start PayPal checkout (HTTP ${response.code()}). Please try again.")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // PayPal: capture the approved order and place the Magento order
    suspend fun payPalPlaceOrder(quoteId: Int, paypalOrderId: String): NetworkResult<PlaceOrderResponse> {
        return try {
            val response = apiService.payPalPlaceOrder(
                PayPalPlaceOrderRequest(
                    quoteId = quoteId,
                    orderId = paypalOrderId
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Order confirmation failed")
                } else {
                    NetworkResult.Success(body)
                }
            } else NetworkResult.Error("Order confirmation failed (HTTP ${response.code()}). If you were charged, please contact support.")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Amazon Pay: open a hosted checkout session for the current quote
    suspend fun createAmazonPayCheckout(quoteId: Int, customerId: Int): NetworkResult<WebCheckoutSessionResponse> {
        return try {
            val response = apiService.createAmazonPayCheckout(WebCheckoutSessionRequest(quoteId, customerId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Could not start Amazon Pay checkout")
                } else {
                    NetworkResult.Success(body)
                }
            } else if (response.code() == 404) NetworkResult.Error("Amazon Pay is not available yet. Please use another payment method.")
            else NetworkResult.Error("Could not start Amazon Pay checkout (HTTP ${response.code()}). Please try again.")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Amazon Pay: complete the approved session and place the Magento order
    suspend fun amazonPayPlaceOrder(referenceId: String): NetworkResult<PlaceOrderResponse> {
        return try {
            val response = apiService.amazonPayPlaceOrder(WebCheckoutPlaceOrderRequest(referenceId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Order confirmation failed")
                } else {
                    NetworkResult.Success(body)
                }
            } else NetworkResult.Error("Order confirmation failed (HTTP ${response.code()}). If you were charged, please contact support.")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Afterpay: open a hosted checkout session for the current quote
    suspend fun createAfterpayCheckout(quoteId: Int, customerId: Int): NetworkResult<WebCheckoutSessionResponse> {
        return try {
            val response = apiService.createAfterpayCheckout(WebCheckoutSessionRequest(quoteId, customerId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Could not start Afterpay checkout")
                } else {
                    NetworkResult.Success(body)
                }
            } else if (response.code() == 404) NetworkResult.Error("Afterpay is not available yet. Please use another payment method.")
            else NetworkResult.Error("Could not start Afterpay checkout (HTTP ${response.code()}). Please try again.")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    // Afterpay: complete the approved session and place the Magento order
    suspend fun afterpayPlaceOrder(referenceId: String): NetworkResult<PlaceOrderResponse> {
        return try {
            val response = apiService.afterpayPlaceOrder(WebCheckoutPlaceOrderRequest(referenceId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.status == false) {
                    NetworkResult.Error(body.message ?: "Order confirmation failed")
                } else {
                    NetworkResult.Success(body)
                }
            } else NetworkResult.Error("Order confirmation failed (HTTP ${response.code()}). If you were charged, please contact support.")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
