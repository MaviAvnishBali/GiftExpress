package com.giftexpress.app.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private val viewModel: ProductDetailsViewModel by viewModels()
    private val args: ProductDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                androidx.compose.material3.MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                    ProductDetailsScreen(
                        sku = args.sku,
                        viewModel = viewModel,
                        onBackClick = {
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }
    }
}
