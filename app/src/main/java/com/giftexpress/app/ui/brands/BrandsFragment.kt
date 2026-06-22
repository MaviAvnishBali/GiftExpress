package com.giftexpress.app.ui.brands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class BrandsFragment : Fragment() {

    private val viewModel: BrandViewModel by viewModels()
    private val cartViewModel: com.giftexpress.app.ui.cart.CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val addedSkus by cartViewModel.addedSkus.collectAsState(initial = emptySet())
                val selectedBrand by viewModel.selectedBrand.collectAsState()

                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                    if (selectedBrand != null) {
                        viewModel.clearSelectedBrand()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }

                MaterialTheme(typography = GiftExpressTypography) {
                        BrandsScreen(
                            viewModel = viewModel,
                            onBackClick = {
                                findNavController().navigateUp()
                            },
                            onBrandClick = { brandId, brandName ->
                                viewModel.selectBrand(brandId, brandName)
                            },
                            onProductClick = { sku ->
                                val bundle = Bundle().apply {
                                    putString("sku", sku)
                                }
                                findNavController().navigate(R.id.productDetailsFragment, bundle)
                            },
                            onCartClick = {
                                findNavController().navigate(R.id.cartFragment)
                            },
                            onMenuClick = {
                                val drawerLayout = requireActivity().findViewById<androidx.drawerlayout.widget.DrawerLayout>(
                                    R.id.drawer_layout)
                                drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
                            },
                            onAddToCart = { sku ->
                                cartViewModel.addProductToCart(sku)
                            },
                            onGoToCart = {
                                findNavController().navigate(R.id.cartFragment)
                            },
                            addedSkus = addedSkus
                        )
                }
            }
        }
    }
}
