package com.giftexpress.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class SearchListingFragment : Fragment() {

    private val viewModel: SearchListingViewModel by viewModels()

    private val cartViewModel: com.giftexpress.app.ui.cart.CartViewModel by activityViewModels()
    private val wishlistViewModel: com.giftexpress.app.ui.wishlist.WishlistViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchString = arguments?.getString("searchString") ?: ""

        return ComposeView(requireContext()).apply {
            setContent {
                val addedSkus by cartViewModel.addedSkus.collectAsState(initial = emptySet())
                MaterialTheme(typography = GiftExpressTypography) {
                    SearchListingScreen(
                        searchString = searchString,
                        viewModel = viewModel,
                        onBackClick = { findNavController().navigateUp() },
                        onProductClick = { sku, imageUrl ->
                            val bundle = bundleOf(
                                "sku" to sku,
                                "fallbackImageUrl" to imageUrl
                            )
                            findNavController().navigate(R.id.productDetailsFragment, bundle)
                        },
                        addedSkus = addedSkus,
                        onAddToCart = { sku ->
                            if (cartViewModel.isLoggedIn()) {
                                cartViewModel.addProductToCart(sku)
                            } else {
                                com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your cart.") {
                                    cartViewModel.setPendingCartSku(sku)
                                    findNavController().navigate(R.id.loginFragment)
                                }
                            }
                        },
                        onGoToCart = {
                            findNavController().navigate(R.id.cartFragment)
                        },
                        onAddToWishlist = { sku ->
                            if (cartViewModel.isLoggedIn()) {
                                wishlistViewModel.addToWishlist(sku)
                                android.widget.Toast.makeText(requireContext(), "Added to wishlist", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your wishlist.") {
                                    cartViewModel.setPendingWishlistSku(sku)
                                    findNavController().navigate(R.id.loginFragment)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
