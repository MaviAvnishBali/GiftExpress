package com.giftexpress.app.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.giftexpress.app.R
import com.giftexpress.app.ui.cart.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val viewModel: ProductDetailsViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val args: ProductDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = findNavController()
                val addedSkus by cartViewModel.addedSkus.collectAsState(initial = emptySet())
                androidx.compose.material3.MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                        ProductDetailsScreen(
                            sku = args.sku,
                            viewModel = viewModel,
                            addedSkus = addedSkus,
                            onBackClick = {
                                navController.navigateUp()
                            },
                            onCartClick = {
                                navController.navigate(R.id.cartFragment)
                            },
                            onProductCardAddToCart = { sku ->
                                cartViewModel.addProductToCart(sku)
                            },
                            onProductCardGoToCart = {
                                navController.navigate(R.id.cartFragment)
                            },
                            onProductClick = { sku ->
                                val bundle = Bundle().apply {
                                    putString("sku", sku)
                                }
                                navController.navigate(R.id.productDetailsFragment, bundle)
                            }
                        )
                }
            }
        }
    }
}
