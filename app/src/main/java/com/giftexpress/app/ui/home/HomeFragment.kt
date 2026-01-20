package com.giftexpress.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.giftexpress.app.R
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.model.SliderResponse
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.giftexpress.app.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home Screen Fragment
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()
        return ComposeView(requireContext()).apply {
            // Dispose the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val addedSkus by cartViewModel.addedSkus.collectAsState(initial = emptySet())
                
                // Observe Cart Events
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    cartViewModel.cartEvents.collect { event ->
                        when (event) {
                            is com.giftexpress.app.ui.cart.CartEvent.ItemAdded -> {
                                android.widget.Toast.makeText(requireContext(), "Added to cart", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                    HomeScreen(
                        viewModel = viewModel,
                        onMenuClick = {
                            val drawerLayout = requireActivity().findViewById<androidx.drawerlayout.widget.DrawerLayout>(
                                R.id.drawer_layout)
                            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
                        },
                        onProductClick = { sku ->
                            val bundle = Bundle().apply {
                                putString("sku", sku)
                            }
                            androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.productDetailsFragment, bundle)
                        },
                        onCategoryClick = { product ->
                            val categoryId = product.categoryId ?: product.id?.toIntOrNull() ?: 0
                            val categoryName = product.name ?: "Category"
                            val bundle = Bundle().apply {
                                putInt("categoryId", categoryId)
                                putString("categoryName", categoryName)
                            }
                            androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(R.id.categoryFragment, bundle)
                        },
                        onAddToCart = { sku ->
                            cartViewModel.addProductToCart(sku)
                        },
                        onGoToCart = { navController.navigate(R.id.cartFragment) },
                        addedSkus = addedSkus,
                        onCartClick = {
                            navController.navigate(R.id.cartFragment)
                        }
                    )
                }
            }
        }
    }
}
