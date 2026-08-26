package com.giftexpress.app.ui.brands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import com.giftexpress.app.ui.products.SpecialProductsViewModel
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class BrandsFragment : Fragment() {

    private val viewModel: BrandViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val specialViewModel: SpecialProductsViewModel by viewModels()
    private val wishlistViewModel: com.giftexpress.app.ui.wishlist.WishlistViewModel by viewModels()

    @javax.inject.Inject
    lateinit var cartCountManager: com.giftexpress.app.data.repository.CartCountManager

    override fun onResume() {
        super.onResume()
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
                    BrandsScreen(
                        viewModel = viewModel,
                        specialViewModel = specialViewModel,
                        onMenuClick = {
                            val drawerLayout = requireActivity().findViewById<androidx.drawerlayout.widget.DrawerLayout>(
                                R.id.drawer_layout)
                            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
                        },
                        onCartClick = { findNavController().navigate(R.id.cartFragment) },
                        onSearchClick = { findNavController().navigate(R.id.searchFragment) },
                        onProductClick = { sku ->
                            val bundle = Bundle().apply { putString("sku", sku) }
                            findNavController().navigate(R.id.productDetailsFragment, bundle)
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
                        addedSkus = addedSkus,
                        cartCount = cartCount,
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
