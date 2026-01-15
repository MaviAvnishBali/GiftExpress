package com.giftexpress.app.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryFragment : Fragment() {

    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val categoryId = arguments?.getInt("categoryId") ?: 0
        val categoryName = arguments?.getString("categoryName") ?: "Category"

        viewModel.fetchCategoryData(categoryId)
        viewModel.fetchProducts(categoryId, reset = true)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
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
                            findNavController().navigate(R.id.cartFragment)
                        }
                    )
                }
            }
        }
    }
}
