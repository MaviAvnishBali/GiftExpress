package com.giftexpress.app.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.PlaceOrderResponse
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.data.repository.CartCountManager
import com.giftexpress.app.data.repository.CartRepository
import com.giftexpress.app.data.repository.CheckoutRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StripePaymentState {
    object Idle : StripePaymentState()
    object Loading : StripePaymentState()
    data class ReadyToPresent(val clientSecret: String, val paymentIntentId: String) : StripePaymentState()
    data class Error(val message: String) : StripePaymentState()
}

sealed class PayPalPaymentState {
    object Idle : PayPalPaymentState()
    object Loading : PayPalPaymentState()
    data class ReadyToLaunch(val orderId: String) : PayPalPaymentState()
    object AwaitingApproval : PayPalPaymentState()
    data class Error(val message: String) : PayPalPaymentState()
}

/**
 * Web-redirect wallets that share a hosted-page approval flow (Amazon Pay, Afterpay).
 * Each carries its own deep-link scheme (registered in the manifest) used to route the
 * browser return back into the app, mirroring the PayPal return scheme.
 */
enum class RedirectProvider(val displayName: String, val returnScheme: String) {
    AMAZON_PAY("Amazon Pay", "com.giftexpress.app.amazonpay"),
    AFTERPAY("Afterpay", "com.giftexpress.app.afterpay")
}

sealed class RedirectPaymentState {
    object Idle : RedirectPaymentState()
    object Loading : RedirectPaymentState()
    data class ReadyToLaunch(
        val provider: RedirectProvider,
        val redirectUrl: String,
        val referenceId: String
    ) : RedirectPaymentState()
    object AwaitingApproval : RedirectPaymentState()
    data class Error(val message: String) : RedirectPaymentState()
}

/** Payment methods shown on the payment screen (mockup 43/51 + Amazon Pay / Afterpay). */
enum class PaymentMethodOption { PAYPAL, GOOGLE_PAY, AMAZON_PAY, AFTERPAY, KLARNA, CARD }

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: CheckoutRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val cartCountManager: CartCountManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _stripeState = MutableStateFlow<StripePaymentState>(StripePaymentState.Idle)
    val stripeState: StateFlow<StripePaymentState> = _stripeState.asStateFlow()

    private val _payPalState = MutableStateFlow<PayPalPaymentState>(PayPalPaymentState.Idle)
    val payPalState: StateFlow<PayPalPaymentState> = _payPalState.asStateFlow()

    private val _redirectState = MutableStateFlow<RedirectPaymentState>(RedirectPaymentState.Idle)
    val redirectState: StateFlow<RedirectPaymentState> = _redirectState.asStateFlow()

    private val _placeOrderState = MutableStateFlow<UiState<PlaceOrderResponse>>(UiState.Idle)
    val placeOrderState: StateFlow<UiState<PlaceOrderResponse>> = _placeOrderState.asStateFlow()

    private val _selectedMethod = MutableStateFlow(PaymentMethodOption.CARD)
    val selectedMethod: StateFlow<PaymentMethodOption> = _selectedMethod.asStateFlow()

    /**
     * PayPal SDK auth state — needed by finishStart() when the app returns from the
     * PayPal browser flow. Kept in SavedStateHandle to survive process death.
     */
    var payPalAuthState: String?
        get() = savedStateHandle["paypal_auth_state"]
        set(value) { savedStateHandle["paypal_auth_state"] = value }

    /**
     * Redirect wallet (Amazon Pay / Afterpay) pending-return state — needed to complete the
     * flow when the app is deep-linked back from the hosted checkout page. Kept in
     * SavedStateHandle to survive process death, matching [payPalAuthState].
     */
    private var pendingRedirectProvider: RedirectProvider?
        get() = savedStateHandle.get<String>("redirect_provider")?.let { runCatching { RedirectProvider.valueOf(it) }.getOrNull() }
        set(value) { savedStateHandle["redirect_provider"] = value?.name }

    private var pendingRedirectReference: String?
        get() = savedStateHandle["redirect_reference"]
        set(value) { savedStateHandle["redirect_reference"] = value }

    fun selectMethod(method: PaymentMethodOption) {
        _selectedMethod.value = method
    }

    /**
     * Step 1 — Get Stripe payment intent.
     * Matches iOS: PaymentViewModel.getPaymentIntent(parameters: ["quoteId": X, "customerId": Y])
     */
    fun fetchPaymentIntent() {
        viewModelScope.launch {
            _stripeState.value = StripePaymentState.Loading
            val quoteId = cartRepository.getOrCreateQuoteId() ?: run {
                _stripeState.value = StripePaymentState.Error("Could not get cart ID")
                return@launch
            }
            val customerId = authRepository.getCustomerId() ?: run {
                _stripeState.value = StripePaymentState.Error("Could not get customer ID")
                return@launch
            }
            when (val result = repository.getStripePaymentIntent(quoteId, customerId)) {
                is NetworkResult.Success -> {
                    val data = result.data!!
                    val secret = data.clientSecret
                    val intentId = data.paymentIntentId
                    if (!secret.isNullOrBlank() && !intentId.isNullOrBlank()) {
                        _stripeState.value = StripePaymentState.ReadyToPresent(secret, intentId)
                    } else {
                        _stripeState.value = StripePaymentState.Error(data.message ?: "Invalid payment intent")
                    }
                }
                is NetworkResult.Error -> _stripeState.value = StripePaymentState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    /**
     * Step 2 — Payment completed in Stripe sheet. Place the order.
     * Matches iOS: PaymentViewModel.getOrderDetails(parameters: ["payment_intent_id": intentId])
     */
    fun confirmOrder(paymentIntentId: String) {
        viewModelScope.launch {
            _placeOrderState.value = UiState.Loading
            when (val result = repository.stripePlaceOrder(paymentIntentId)) {
                is NetworkResult.Success -> result.data?.let {
                    cartRepository.clearQuoteId()
                    cartCountManager.reset()
                    _placeOrderState.value = UiState.Success(it)
                } ?: run { _placeOrderState.value = UiState.Error("Empty response") }
                is NetworkResult.Error -> _placeOrderState.value = UiState.Error(result.message ?: "Order failed")
                else -> {}
            }
        }
    }

    fun onPaymentCanceled() {
        _stripeState.value = StripePaymentState.Idle
    }

    fun onPaymentFailed(message: String) {
        _stripeState.value = StripePaymentState.Error(message)
    }

    // ─── PayPal flow ─────────────────────────────────────────────────────────

    /**
     * PayPal Step 1 — Ask the backend to create a PayPal order for this quote.
     * The returned paypal_order_id is handed to the PayPal SDK for approval.
     */
    fun startPayPalCheckout(totalAmount: Double) {
        viewModelScope.launch {
            _payPalState.value = PayPalPaymentState.Loading
            val quoteId = cartRepository.getOrCreateQuoteId() ?: run {
                _payPalState.value = PayPalPaymentState.Error("Could not get cart ID")
                return@launch
            }
            val formattedAmount = String.format("%.2f", totalAmount)
            when (val result = repository.createPayPalOrder(quoteId, formattedAmount)) {
                is NetworkResult.Success -> {
                    val orderId = result.data?.paypalOrderId
                    if (!orderId.isNullOrBlank()) {
                        _payPalState.value = PayPalPaymentState.ReadyToLaunch(orderId)
                    } else {
                        _payPalState.value = PayPalPaymentState.Error(result.data?.message ?: "Invalid PayPal order")
                    }
                }
                is NetworkResult.Error -> _payPalState.value = PayPalPaymentState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    /** PayPal browser flow launched — remember the SDK auth state for finishStart(). */
    fun onPayPalCheckoutLaunched(authState: String) {
        payPalAuthState = authState
        _payPalState.value = PayPalPaymentState.AwaitingApproval
    }

    /**
     * PayPal Step 2 — Buyer approved in the browser. Capture + place the order on the backend.
     */
    fun onPayPalApproved(paypalOrderId: String) {
        payPalAuthState = null
        _payPalState.value = PayPalPaymentState.Idle
        viewModelScope.launch {
            _placeOrderState.value = UiState.Loading
            val quoteId = cartRepository.getOrCreateQuoteId() ?: 0
            when (val result = repository.payPalPlaceOrder(quoteId, paypalOrderId)) {
                is NetworkResult.Success -> result.data?.let {
                    cartRepository.clearQuoteId()
                    cartCountManager.reset()
                    _placeOrderState.value = UiState.Success(it)
                } ?: run { _placeOrderState.value = UiState.Error("Empty response") }
                is NetworkResult.Error -> _placeOrderState.value = UiState.Error(result.message ?: "Order failed")
                else -> {}
            }
        }
    }

    fun onPayPalCanceled() {
        payPalAuthState = null
        _payPalState.value = PayPalPaymentState.Idle
    }

    fun onPayPalFailed(message: String) {
        payPalAuthState = null
        _payPalState.value = PayPalPaymentState.Error(message)
    }

    // ─── Amazon Pay / Afterpay (shared web-redirect flow) ────────────────────────

    /**
     * Redirect Step 1 — Ask the backend to open a hosted checkout session for this quote.
     * The returned redirect_url is opened in a browser tab for the buyer to approve.
     */
    fun startRedirectCheckout(provider: RedirectProvider) {
        viewModelScope.launch {
            _redirectState.value = RedirectPaymentState.Loading
            val quoteId = cartRepository.getOrCreateQuoteId() ?: run {
                _redirectState.value = RedirectPaymentState.Error("Could not get cart ID")
                return@launch
            }
            val customerId = authRepository.getCustomerId() ?: run {
                _redirectState.value = RedirectPaymentState.Error("Could not get customer ID")
                return@launch
            }
            val result = when (provider) {
                RedirectProvider.AMAZON_PAY -> repository.createAmazonPayCheckout(quoteId, customerId)
                RedirectProvider.AFTERPAY -> repository.createAfterpayCheckout(quoteId, customerId)
            }
            when (result) {
                is NetworkResult.Success -> {
                    val data = result.data
                    val url = data?.redirectUrl
                    val reference = data?.referenceId
                    if (!url.isNullOrBlank() && !reference.isNullOrBlank()) {
                        _redirectState.value = RedirectPaymentState.ReadyToLaunch(provider, url, reference)
                    } else {
                        _redirectState.value = RedirectPaymentState.Error(data?.message ?: "Invalid ${provider.displayName} session")
                    }
                }
                is NetworkResult.Error -> _redirectState.value = RedirectPaymentState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    /** Hosted checkout page launched — remember what to complete when the browser returns. */
    fun onRedirectCheckoutLaunched(provider: RedirectProvider, referenceId: String) {
        pendingRedirectProvider = provider
        pendingRedirectReference = referenceId
        _redirectState.value = RedirectPaymentState.AwaitingApproval
    }

    /** True when a redirect wallet flow is waiting for the browser to deep-link back. */
    fun hasPendingRedirect(): Boolean =
        pendingRedirectProvider != null && !pendingRedirectReference.isNullOrBlank()

    /** The deep-link scheme the pending redirect flow expects to return on, if any. */
    fun pendingRedirectScheme(): String? = pendingRedirectProvider?.returnScheme

    /**
     * Redirect Step 2 — Buyer approved on the hosted page. Complete the session and place the order.
     */
    fun onRedirectApproved() {
        val provider = pendingRedirectProvider ?: return
        val reference = pendingRedirectReference ?: return
        pendingRedirectProvider = null
        pendingRedirectReference = null
        _redirectState.value = RedirectPaymentState.Idle
        viewModelScope.launch {
            _placeOrderState.value = UiState.Loading
            val result = when (provider) {
                RedirectProvider.AMAZON_PAY -> repository.amazonPayPlaceOrder(reference)
                RedirectProvider.AFTERPAY -> repository.afterpayPlaceOrder(reference)
            }
            when (result) {
                is NetworkResult.Success -> result.data?.let {
                    cartRepository.clearQuoteId()
                    cartCountManager.reset()
                    _placeOrderState.value = UiState.Success(it)
                } ?: run { _placeOrderState.value = UiState.Error("Empty response") }
                is NetworkResult.Error -> _placeOrderState.value = UiState.Error(result.message ?: "Order failed")
                else -> {}
            }
        }
    }

    fun onRedirectCanceled() {
        pendingRedirectProvider = null
        pendingRedirectReference = null
        _redirectState.value = RedirectPaymentState.Idle
    }

    fun onRedirectFailed(message: String) {
        pendingRedirectProvider = null
        pendingRedirectReference = null
        _redirectState.value = RedirectPaymentState.Error(message)
    }

    fun resetState() {
        _stripeState.value = StripePaymentState.Idle
        _payPalState.value = PayPalPaymentState.Idle
        _redirectState.value = RedirectPaymentState.Idle
        _placeOrderState.value = UiState.Idle
    }
}
