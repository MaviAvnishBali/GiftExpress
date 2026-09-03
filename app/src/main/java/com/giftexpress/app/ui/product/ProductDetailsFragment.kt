package com.giftexpress.app.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import android.widget.Toast
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import com.giftexpress.app.ui.wishlist.WishlistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val viewModel: ProductDetailsViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val wishlistViewModel: WishlistViewModel by viewModels()
    private val args: ProductDetailsFragmentArgs by navArgs()

    @javax.inject.Inject
    lateinit var cartCountManager: com.giftexpress.app.data.repository.CartCountManager

    override fun onResume() {
        super.onResume()
        // Keep the top-bar cart badge in sync with the server cart (silent).
        cartViewModel.syncCartCount()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                val addedSkus by cartViewModel.addedSkus.collectAsState(initial = emptySet())
                val cartCount by cartCountManager.count.collectAsState(initial = 0)

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    cartViewModel.cartEvents.collect { event ->
                        when (event) {
                            is com.giftexpress.app.ui.cart.CartEvent.ItemAdded -> {
                                android.widget.Toast.makeText(requireContext(), "Added to cart", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val cartError by cartViewModel.error.collectAsState()
                androidx.compose.runtime.LaunchedEffect(cartError) {
                    cartError?.let {
                        android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                androidx.compose.material3.MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                        ProductDetailsScreen(
                            sku = args.sku,
                            fallbackImageUrl = args.fallbackImageUrl,
                            viewModel = viewModel,
                            addedSkus = addedSkus,
                            cartCount = cartCount,
                            onBackClick = {
                                navController.navigateUp()
                            },
                            onCartClick = {
                                navController.navigate(R.id.cartFragment)
                            },
                            onSearchClick = {
                                navController.navigate(R.id.searchFragment)
                            },
                            onMainAddToCart = { sku, qty ->
                                if (cartViewModel.isLoggedIn()) {
                                    viewModel.addToCart(sku, qty)
                                } else {
                                    com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your cart.") {
                                        cartViewModel.setPendingCartSku(sku)
                                        navController.navigate(R.id.loginFragment)
                                    }
                                }
                            },
                            onProductCardAddToCart = { sku ->
                                if (cartViewModel.isLoggedIn()) {
                                    cartViewModel.addProductToCart(sku)
                                } else {
                                    com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your cart.") {
                                        cartViewModel.setPendingCartSku(sku)
                                        navController.navigate(R.id.loginFragment)
                                    }
                                }
                            },
                            onProductCardGoToCart = {
                                navController.navigate(R.id.cartFragment)
                            },
                            onProductClick = { sku ->
                                val bundle = Bundle().apply {
                                    putString("sku", sku)
                                }
                                navController.navigate(R.id.productDetailsFragment, bundle)
                            },
                            onAddToWishlist = { skuToAdd ->
                                if (cartViewModel.isLoggedIn()) {
                                    wishlistViewModel.addToWishlist(skuToAdd)
                                    Toast.makeText(requireContext(), "Added to wishlist", Toast.LENGTH_SHORT).show()
                                } else {
                                    com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your wishlist.") {
                                        cartViewModel.setPendingWishlistSku(skuToAdd)
                                        navController.navigate(R.id.loginFragment)
                                    }
                                }
                            }
                        )
                }
            }
        }
    }
}
