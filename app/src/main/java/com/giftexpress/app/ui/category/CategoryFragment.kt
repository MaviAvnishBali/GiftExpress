package com.giftexpress.app.ui.category

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
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private val viewModel: CategoryViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val wishlistViewModel: com.giftexpress.app.ui.wishlist.WishlistViewModel by viewModels()

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
        val navController = findNavController()
        val categoryId = arguments?.getInt("categoryId") ?: 0
        val categoryName = arguments?.getString("categoryName") ?: "Category"

        viewModel.fetchCategoryData(categoryId)
        viewModel.fetchProducts(categoryId, reset = true)

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

                MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                        CategoryScreen(
                            categoryId = categoryId,
                            categoryName = categoryName,
                            viewModel = viewModel,
                            cartCount = cartCount,
                            onSearchClick = { navController.navigate(R.id.searchFragment) },
                            onBackClick = { findNavController().navigateUp() },
                            onProductClick = { sku ->
                                val bundle = Bundle().apply {
                                    putString("sku", sku)
                                }
                                findNavController().navigate(R.id.productDetailsFragment, bundle)
                            },
                            onCartClick = {
                                navController.navigate(R.id.cartFragment)
                            },
                            onAddToCart = { sku ->
                                if (cartViewModel.isLoggedIn()) {
                                    cartViewModel.addProductToCart(sku)
                                } else {
                                    com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your cart.") {
                                        cartViewModel.setPendingCartSku(sku)
                                        navController.navigate(R.id.loginFragment)
                                    }
                                }
                            },
                            onGoToCart = {
                                navController.navigate(R.id.cartFragment)
                            },
                            addedSkus = addedSkus,
                            onSubCategoryClick = { subCatId, subCatName ->
                                val bundle = Bundle().apply {
                                    putInt("categoryId", subCatId)
                                    putString("categoryName", subCatName)
                                }
                                findNavController().navigate(R.id.categoryFragment, bundle)
                            },
                            // Top Sellers / Featured / New Arrivals shortcut → category-scoped
                            // special listing (matches iOS products/{categoryId}?special_flag=X)
                            onSpecialClick = { specialFlag, title ->
                                val bundle = Bundle().apply {
                                    putInt("specialFlag", specialFlag)
                                    putString("title", title)
                                    putInt("categoryId", categoryId)
                                }
                                findNavController().navigate(R.id.specialProductsFragment, bundle)
                            },
                            // "Shop By Category" section arrow → Categories tab (matches Home)
                            onCategoryArrowClick = {
                                navController.navigate(R.id.categoriesFragment)
                            },
                            onBrandArrowClick = {
                                navController.navigate(R.id.offersFragment)
                            },
                            onBrandItemClick = { brandId, brandName ->
                                val bundle = Bundle().apply {
                                    putInt("specialFlag", 0)
                                    putInt("brandId", brandId)
                                    putString("title", brandName)
                                }
                                findNavController().navigate(R.id.specialProductsFragment, bundle)
                            },
                            onAddToWishlist = { skuToAdd ->
                                if (cartViewModel.isLoggedIn()) {
                                    wishlistViewModel.addToWishlist(skuToAdd)
                                    android.widget.Toast.makeText(requireContext(), "Added to wishlist", android.widget.Toast.LENGTH_SHORT).show()
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
