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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private val viewModel: CategoryViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

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
                MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                    CategoryScreen(
                        categoryId = categoryId,
                        categoryName = categoryName,
                        viewModel = viewModel,
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
                            cartViewModel.addProductToCart(sku)
                        },
                        onGoToCart = {
                            navController.navigate(R.id.cartFragment)
                        },
                        addedSkus = addedSkus
                    )
                }
            }
        }
    }
}
