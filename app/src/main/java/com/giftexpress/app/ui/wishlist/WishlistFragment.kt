package com.giftexpress.app.ui.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import com.giftexpress.app.ui.theme.GiftExpressTypography
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WishlistFragment : Fragment() {

    private val viewModel: WishlistViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    val addToWishlistState by viewModel.addToWishlistState.collectAsState()

                    LaunchedEffect(addToWishlistState) {
                        when (addToWishlistState) {
                            is UiState.Error -> {
                                Toast.makeText(
                                    requireContext(),
                                    (addToWishlistState as UiState.Error).message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.resetAddState()
                            }
                            else -> {}
                        }
                    }

                    val addedSkus by cartViewModel.addedSkus.collectAsState()

                    WishlistScreen(
                        viewModel = viewModel,
                        addedSkus = addedSkus,
                        onBackClick = { findNavController().navigateUp() },
                        onProductClick = { sku ->
                            val bundle = Bundle().apply { putString("sku", sku) }
                            findNavController().navigate(R.id.productDetailsFragment, bundle)
                        },
                        onAddToCart = { sku ->
                            if (cartViewModel.isLoggedIn()) {
                                cartViewModel.addProductToCart(sku)
                                Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show()
                            } else {
                                com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your cart.") {
                                    cartViewModel.setPendingCartSku(sku)
                                    findNavController().navigate(R.id.loginFragment)
                                }
                            }
                        },
                        onGoToCart = {
                            findNavController().navigate(R.id.cartFragment)
                        }
                    )
                }
            }
        }
    }
}
