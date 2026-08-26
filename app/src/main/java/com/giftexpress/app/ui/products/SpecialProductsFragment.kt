package com.giftexpress.app.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpecialProductsFragment : Fragment() {

    private val viewModel: SpecialProductsViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val wishlistViewModel: com.giftexpress.app.ui.wishlist.WishlistViewModel by viewModels()
    private val args: SpecialProductsFragmentArgs by navArgs()

    @javax.inject.Inject
    lateinit var cartCountManager: com.giftexpress.app.data.repository.CartCountManager

    override fun onResume() {
        super.onResume()
        // Keep the top-bar cart badge in sync with the server cart (silent — no toast/spinner).
        cartViewModel.syncCartCount()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
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

                MaterialTheme(typography = GiftExpressTypography) {
                    SpecialProductsScreen(
                        specialFlag = args.specialFlag,
                        title = args.title,
                        categoryId = args.categoryId,
                        brandId = args.brandId,
                        viewModel = viewModel,
                        addedSkus = addedSkus,
                        cartCount = cartCount,
                        onBackClick = { findNavController().navigateUp() },
                        onProductClick = { sku ->
                            val action = SpecialProductsFragmentDirections
                                .actionSpecialProductsFragmentToProductDetailsFragment(sku)
                            findNavController().navigate(action)
                        },
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
                        onGoToCart = { findNavController().navigate(R.id.cartFragment) },
                        onSearchClick = { findNavController().navigate(R.id.searchFragment) },
                        onCartClick = { findNavController().navigate(R.id.cartFragment) },
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
                        },
                        bannerUrl = args.bannerUrl
                    )
                }
            }
        }
    }
}
