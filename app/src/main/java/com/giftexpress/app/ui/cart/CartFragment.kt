package com.giftexpress.app.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.compose.material3.MaterialTheme
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private val viewModel: CartViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchCart()
    }

    override fun onResume() {
        super.onResume()
        // Refresh cart every time fragment becomes visible (matches iOS viewWillAppear)
        viewModel.fetchCart()
    }

    override fun onCreateView(
        LayoutInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                        CartScreen(
                            viewModel = viewModel,
                            onBackClick = { findNavController().navigateUp() },
                            onCheckout = { findNavController().navigate(R.id.checkoutFragment) },
                            onProductClick = { sku ->
                                val bundle = Bundle().apply { putString("sku", sku) }
                                findNavController().navigate(R.id.productDetailsFragment, bundle)
                            },
                            onContinueShopping = {
                                findNavController().popBackStack(R.id.homeFragment, false)
                            }
                        )
                }
            }
        }
    }
}
