package com.giftexpress.app.ui.payment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.BuildConfig
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFinishStartResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentFragment : Fragment() {

    companion object {
        // Must match the <data android:scheme> registered on MainActivity in the manifest
        private const val PAYPAL_URL_SCHEME = "com.giftexpress.app.paypal"
    }

    private val viewModel: PaymentViewModel by viewModels()

    // PaymentSheet MUST be created in onCreate — before the view is created
    private lateinit var paymentSheet: PaymentSheet

    private lateinit var payPalClient: PayPalWebCheckoutClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Stripe PaymentSheet with result callback
        paymentSheet = PaymentSheet(this) { result ->
            handlePaymentResult(result)
        }

        // Initialize PayPal web checkout client (browser-based approval flow)
        val payPalEnvironment =
            if (BuildConfig.PAYPAL_ENVIRONMENT == "live") Environment.LIVE else Environment.SANDBOX
        payPalClient = PayPalWebCheckoutClient(
            requireContext().applicationContext,
            CoreConfig(BuildConfig.PAYPAL_CLIENT_ID, payPalEnvironment),
            PAYPAL_URL_SCHEME
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val total = (arguments?.getFloat("total") ?: 0f).toDouble()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    val stripeState by viewModel.stripeState.collectAsState()
                    val payPalState by viewModel.payPalState.collectAsState()
                    val redirectState by viewModel.redirectState.collectAsState()
                    val placeOrderState by viewModel.placeOrderState.collectAsState()
                    val selectedMethod by viewModel.selectedMethod.collectAsState()

                    // When payment intent is ready, present Stripe PaymentSheet
                    LaunchedEffect(stripeState) {
                        if (stripeState is StripePaymentState.ReadyToPresent) {
                            val state = stripeState as StripePaymentState.ReadyToPresent
                            presentStripeSheet(state.clientSecret)
                        }
                    }

                    // When the PayPal order is created, open the PayPal approval flow
                    LaunchedEffect(payPalState) {
                        if (payPalState is PayPalPaymentState.ReadyToLaunch) {
                            val state = payPalState as PayPalPaymentState.ReadyToLaunch
                            launchPayPalCheckout(state.orderId)
                        }
                    }

                    // When an Amazon Pay / Afterpay session is created, open the hosted page
                    LaunchedEffect(redirectState) {
                        if (redirectState is RedirectPaymentState.ReadyToLaunch) {
                            val state = redirectState as RedirectPaymentState.ReadyToLaunch
                            launchRedirectCheckout(state.provider, state.redirectUrl, state.referenceId)
                        }
                    }

                    // When order is confirmed, navigate to success
                    LaunchedEffect(placeOrderState) {
                        if (placeOrderState is com.giftexpress.app.utils.UiState.Success) {
                            val response = (placeOrderState as com.giftexpress.app.utils.UiState.Success).data
                            val orderId = response.getDisplayOrderId()
                            viewModel.resetState()
                            findNavController().navigate(
                                R.id.orderSuccessFragment,
                                bundleOf("orderId" to orderId)
                            )
                        }
                    }

                    PaymentScreen(
                        total = total,
                        stripeState = stripeState,
                        payPalState = payPalState,
                        redirectState = redirectState,
                        placeOrderState = placeOrderState,
                        selectedMethod = selectedMethod,
                        onMethodSelected = { viewModel.selectMethod(it) },
                        onBackClick = { findNavController().navigateUp() },
                        onPayClick = {
                            when (viewModel.selectedMethod.value) {
                                PaymentMethodOption.PAYPAL -> viewModel.startPayPalCheckout(total)
                                // Google Pay, Klarna, Afterpay, Amazon Pay, & cards are all presented through the Stripe sheet
                                PaymentMethodOption.GOOGLE_PAY,
                                PaymentMethodOption.KLARNA,
                                PaymentMethodOption.AFTERPAY,
                                PaymentMethodOption.AMAZON_PAY,
                                PaymentMethodOption.CARD -> viewModel.fetchPaymentIntent()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Completes the PayPal flow after the browser deep-links back into MainActivity
        // (MainActivity.onNewIntent stores the deep link via setIntent)
        checkForPayPalResult(requireActivity().intent)
        // Completes the Amazon Pay / Afterpay hosted-page flow on the same deep-link return
        checkForRedirectResult(requireActivity().intent)
    }

    /**
     * Present Stripe's native PaymentSheet — matches iOS paymentSheet?.present(from: self)
     */
    private fun presentStripeSheet(clientSecret: String) {
        val googlePayEnvironment = if (BuildConfig.PAYPAL_ENVIRONMENT == "live") {
            PaymentSheet.GooglePayConfiguration.Environment.Production
        } else {
            PaymentSheet.GooglePayConfiguration.Environment.Test
        }
        val googlePayConfig = PaymentSheet.GooglePayConfiguration(
            environment = googlePayEnvironment,
            countryCode = "US",
            currencyCode = "USD"
        )
        val config = PaymentSheet.Configuration.Builder("GiftExpress")
            .googlePay(googlePayConfig)
            .build()
        paymentSheet.presentWithPaymentIntent(clientSecret, config)
    }

    /**
     * Handle Stripe PaymentSheet result — matches iOS switch paymentResult { case .completed, .canceled, .failed }
     */
    private fun handlePaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Payment success — place the order with the payment intent ID
                val state = viewModel.stripeState.value
                if (state is StripePaymentState.ReadyToPresent) {
                    viewModel.confirmOrder(state.paymentIntentId)
                }
            }
            is PaymentSheetResult.Canceled -> {
                viewModel.onPaymentCanceled()
            }
            is PaymentSheetResult.Failed -> {
                viewModel.onPaymentFailed(result.error.message ?: "Payment failed. Please try again.")
            }
        }
    }

    /**
     * Open PayPal approval in a Chrome Custom Tab. The buyer approves the order on
     * paypal.com and is deep-linked back into the app (see manifest intent filter).
     */
    private fun launchPayPalCheckout(paypalOrderId: String) {
        val request = PayPalWebCheckoutRequest(paypalOrderId, PayPalWebCheckoutFundingSource.PAYPAL)
        viewModel.onPayPalCheckoutLaunched("launched")
        payPalClient.start(requireActivity(), request)
    }

    /**
     * Balancing call for payPalClient.start() — resolves the outcome of the browser flow.
     */
    private fun checkForPayPalResult(intent: Intent) {
        val data = intent.data ?: return
        if (!data.scheme.equals(PAYPAL_URL_SCHEME, ignoreCase = true)) return
        when (val result = payPalClient.finishStart(intent)) {
            is PayPalWebCheckoutFinishStartResult.Success -> {
                val orderId = result.orderId
                if (!orderId.isNullOrBlank()) {
                    // Buyer approved — capture & place order on the backend
                    viewModel.onPayPalApproved(orderId)
                } else {
                    viewModel.onPayPalFailed("PayPal did not return an order ID.")
                }
            }
            is PayPalWebCheckoutFinishStartResult.Failure ->
                viewModel.onPayPalFailed(result.error.message ?: "PayPal payment failed. Please try again.")
            is PayPalWebCheckoutFinishStartResult.Canceled ->
                viewModel.onPayPalCanceled()
            PayPalWebCheckoutFinishStartResult.NoResult ->
                viewModel.onPayPalCanceled()
            else -> {}
        }
        // Clear intent data after handling
        requireActivity().intent = Intent()
    }

    // ─── Amazon Pay / Afterpay (hosted-page web redirect) ───────────────────────

    /**
     * Open the provider's hosted checkout page in a Chrome Custom Tab. The buyer approves
     * there and is deep-linked back into the app on the provider's return scheme (see the
     * intent filters in the manifest), which [checkForRedirectResult] resolves.
     */
    private fun launchRedirectCheckout(
        provider: RedirectProvider,
        redirectUrl: String,
        referenceId: String
    ) {
        // Record the pending return BEFORE launching so the flow survives process death.
        viewModel.onRedirectCheckoutLaunched(provider, referenceId)
        try {
            CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(redirectUrl))
        } catch (e: Exception) {
            viewModel.onRedirectFailed("Could not open ${provider.displayName}. Please try again.")
        }
    }

    /**
     * Resolve the outcome of the hosted-page flow when the browser deep-links back.
     * The backend controls the return URL; we treat an explicit cancel/failure status as
     * such and otherwise hand off to the backend place-order step to verify the session.
     */
    private fun checkForRedirectResult(intent: Intent) {
        if (!viewModel.hasPendingRedirect()) return
        val data = intent.data ?: return
        // Only act on the scheme the pending provider is waiting for.
        if (!data.scheme.equals(viewModel.pendingRedirectScheme(), ignoreCase = true)) return

        when (data.getQueryParameter("status")?.lowercase()) {
            "cancel", "canceled", "cancelled" -> viewModel.onRedirectCanceled()
            "failure", "failed", "error" -> viewModel.onRedirectFailed("Payment was not completed. Please try again.")
            else -> viewModel.onRedirectApproved()
        }
        // Consume the deep link so a config change / re-resume doesn't reprocess it.
        requireActivity().intent = Intent()
    }
}
