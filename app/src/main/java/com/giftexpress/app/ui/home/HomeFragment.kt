package com.giftexpress.app.ui.home

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
import com.giftexpress.app.data.model.SliderBanner
import com.giftexpress.app.data.model.SliderOffer
import com.giftexpress.app.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home Screen Fragment — all navigation matches iOS HomeViewController
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private val wishlistViewModel: com.giftexpress.app.ui.wishlist.WishlistViewModel by viewModels()

    @javax.inject.Inject
    lateinit var cartCountManager: com.giftexpress.app.data.repository.CartCountManager

    override fun onResume() {
        super.onResume()
        // Refresh the header cart badge from the server whenever Home becomes visible
        // (e.g. after adding items on another screen). Silent — no spinner/toast here.
        cartViewModel.syncCartCount()
    }

    // iOS enum constants
    private val CATEGORY_WOMEN = 4
    private val CATEGORY_MEN = 3
    private val BRAND_AHUJA = 5049
    private val FLAG_BEST_SELLING = 15
    private val FLAG_NEW_ARRIVAL = 17
    private val FLAG_FEATURED = 14

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()
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
                    HomeScreen(
                        viewModel = viewModel,

                        onMenuClick = {
                            val drawerLayout = requireActivity().findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
                            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
                        },

                        onProductClick = { sku -> navigateToProduct(sku) },

                        onCategoryClick = { product ->
                            // Route by category_id: the real categories (Women's/Men's/Gift Sets/
                            // Unisex — type "category") carry a category_id and open the category
                            // landing page with its banner + sub-sections. Best Sellers is a
                            // "custom_url" tile with no category_id, so it opens the best-sellers
                            // listing instead (no category banner to show).
                            val categoryId = product.categoryId ?: 0
                            val name = product.name ?: "Category"
                            if (categoryId > 0) {
                                navigateToCategory(categoryId, name)
                            } else {
                                navigateToSpecialProducts(FLAG_BEST_SELLING, name, null)
                            }
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
                        onGoToCart = { navController.navigate(R.id.cartFragment) },
                        addedSkus = addedSkus,
                        onCartClick = { navController.navigate(R.id.cartFragment) },
                        onSearchClick = { navController.navigate(R.id.searchFragment) },

                        // Slider section arrow — matches iOS cell.arrowTapped by title
                        onSeeAllClick = { specialFlag, title ->
                            navigateToSpecialProducts(specialFlag, title)
                        },

                        // Non-special slider arrow (Men's / Women's Fragrances, Ahuja Products).
                        // Routed by title: brand sliders → brand listing, category sliders → category listing.
                        onSliderSeeAllClick = { categoryId, title ->
                            when {
                                // Ahuja opens on the same unified product-listing screen as
                                // Featured / Best Sellers / New Arrivals, but loads the Ahuja
                                // brand catalog (brandId) instead of a special_flag list.
                                title.contains("ahuja", ignoreCase = true) ->
                                    navigateToBrandProducts(BRAND_AHUJA, title)
                                // check "women" before "men" — "men" is a substring of "women"
                                title.contains("women", ignoreCase = true) ->
                                    navigateToCategory(categoryId ?: CATEGORY_WOMEN, title)
                                title.contains("men", ignoreCase = true) ->
                                    navigateToCategory(categoryId ?: CATEGORY_MEN, title)
                                // Any other category-style slider that carries a category_id
                                categoryId != null -> navigateToCategory(categoryId, title)
                            }
                        },

                        // Category section arrow → Categories tab (iOS: tabBarController.selectedIndex = 1)
                        onCategoryArrowClick = {
                            navController.navigate(R.id.categoriesFragment)
                        },

                        // Brand section arrow → Brands tab (iOS: tabBarController.selectedIndex = 2)
                        onBrandArrowClick = {
                            navController.navigate(R.id.offersFragment)
                        },

                        // Brand item tap → brand product listing (iOS: navigateToProductLiting)
                        onBrandItemClick = { brand ->
                            val brandId = brand.id?.toIntOrNull() ?: brand.productId ?: brand.categoryId ?: 0
                            val fallbackName = brand.url?.substringAfterLast("/")?.removeSuffix(".html")
                                ?.replace("-", " ")
                                ?.split(" ")
                                ?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
                            val brandName = brand.name ?: fallbackName ?: "Brand"
                            // Do not pass brand.image as bannerUrl, it causes an oversized unneeded header
                            navigateToBrandProducts(brandId, brandName, null)
                        },

                        // Banner tap — type-based navigation (iOS: onBannerTap switch type)
                        onBannerClick = { banner -> handleBannerTap(banner) },

                        // Offer tap — type-based navigation (iOS: onOfferTapped)
                        onOfferClick = { offer -> handleOfferTap(offer) },

                        // Cart badge count — backed by a shared, app-wide CartCountManager so the
                        // header cart icon updates immediately after "Add to cart" from anywhere
                        // (home cards, product details) and survives navigation.
                        cartCount = cartCount,

                        onAddToWishlist = { sku ->
                            if (cartViewModel.isLoggedIn()) {
                                wishlistViewModel.addToWishlist(sku)
                                android.widget.Toast.makeText(requireContext(), "Added to wishlist", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                com.giftexpress.app.utils.showLoginRequiredDialog(requireContext(), "Please login to add items to your wishlist.") {
                                    cartViewModel.setPendingWishlistSku(sku)
                                    navController.navigate(R.id.loginFragment)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // ── Navigation helpers matching iOS methods ────────────────────────────────

    /** Matches iOS navigateToProductScreen / navigateToProductScreenFromBanner */
    private fun navigateToProduct(sku: String) {
        val bundle = Bundle().apply { putString("sku", sku) }
        findNavController().navigate(R.id.productDetailsFragment, bundle)
    }

    /** Matches iOS navigateToCategoryListingFromArrow */
    private fun navigateToCategory(categoryId: Int, title: String) {
        val bundle = Bundle().apply {
            putInt("categoryId", categoryId)
            putString("categoryName", title)
        }
        findNavController().navigate(R.id.categoryFragment, bundle)
    }

    /** Matches iOS navigateToProductLitingForBrand with specialFlag */
    private fun navigateToSpecialProducts(specialFlag: Int, title: String, bannerUrl: String? = null) {
        val bundle = Bundle().apply {
            putInt("specialFlag", specialFlag)
            putString("title", title)
            if (!bannerUrl.isNullOrBlank()) {
                putString("bannerUrl", bannerUrl)
            }
        }
        findNavController().navigate(R.id.specialProductsFragment, bundle)
    }

    /**
     * Matches iOS navigateToProductLiting(brand) → GET giftexpress/brand/{id}/products.
     * Uses the unified product-listing screen (SpecialProductsFragment) scoped to a brand, so
     * brand catalogs and the special-flag listings (Featured / Best Sellers / New Arrivals)
     * all share one screen. specialFlag is 0 (unused when brandId > 0).
     */
    private fun navigateToBrandProducts(brandId: Int, title: String, bannerUrl: String? = null) {
        val bundle = Bundle().apply {
            putInt("specialFlag", 0)
            putInt("brandId", brandId)
            putString("title", title)
            if (!bannerUrl.isNullOrBlank()) {
                putString("bannerUrl", bannerUrl)
            }
        }
        findNavController().navigate(R.id.specialProductsFragment, bundle)
    }

    /**
     * iOS: switch banner.type { case "product", "category", "brand" }
     */
    private fun handleBannerTap(banner: SliderBanner) {
        // Matches iOS HomeViewController.onBannerTap: read type/urlApi with "" defaults and
        // switch on type. Empty/null type is a decorative banner — intentionally no action.
        val type = banner.type?.trim()?.lowercase() ?: ""
        val urlApi = banner.urlApi?.trim()?.takeIf { it.isNotBlank() && it != "#" } 
            ?: banner.url?.trim()?.takeIf { it.isNotBlank() && it != "#" } 
            ?: ""
        val title = banner.title ?: ""
        
        // Fallback 1: If type is missing but urlApi looks like a product SKU
        var resolvedType = if (type.isEmpty() && urlApi.startsWith("GXP-", ignoreCase = true)) {
            "product"
        } else {
            type
        }
        
        var resolvedUrlApi = urlApi
        
        // Fallback 2: Check alternative ID fields if type is still missing
        if (resolvedType.isEmpty()) {
            if (banner.categoryId != null && banner.categoryId > 0) {
                resolvedType = "category"
                resolvedUrlApi = banner.categoryId.toString()
            } else if (banner.productId != null && banner.productId > 0) {
                resolvedType = "product"
                resolvedUrlApi = banner.productId.toString()
            }
        }

        when (resolvedType) {
            "product" -> if (resolvedUrlApi.isNotBlank()) navigateToProduct(resolvedUrlApi)
            "category" -> {
                val categoryId = resolvedUrlApi.toIntOrNull() ?: 0
                val fallbackTitle = if (title.isBlank()) {
                    when (categoryId) {
                        CATEGORY_WOMEN -> "Women's Fragrances"
                        CATEGORY_MEN -> "Men's Fragrances"
                        else -> title
                    }
                } else title
                navigateToCategory(categoryId, fallbackTitle)
            }
            "brand" -> navigateToBrandProducts(resolvedUrlApi.toIntOrNull() ?: 0, title)
            else -> android.util.Log.i("HomeBanner", "Banner has no action, type='$resolvedType', url_api='$resolvedUrlApi', full_banner=$banner")
        }
    }

    /**
     * iOS: if offer.type == "category" → categoryListing else → productDetails
     */
    private fun handleOfferTap(offer: SliderOffer) {
        val urlApi = offer.urlApi.trim().takeIf { it.isNotBlank() } ?: ""
        if (urlApi.isBlank()) return
        
        val type = offer.type.trim().lowercase()
        if (type == "category") {
            val categoryId = urlApi.toIntOrNull() ?: return
            val title = when (categoryId) {
                CATEGORY_WOMEN -> "Women's Fragrances"
                CATEGORY_MEN -> "Men's Fragrances"
                else -> "" // Leave empty if unknown, though ideally backend provides it
            }
            navigateToCategory(categoryId, title)
        } else {
            navigateToProduct(urlApi)
        }
    }
}
