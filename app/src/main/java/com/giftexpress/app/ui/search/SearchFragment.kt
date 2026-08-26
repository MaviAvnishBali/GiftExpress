package com.giftexpress.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    SearchScreen(
                        viewModel = viewModel,
                        onBackClick = { findNavController().navigateUp() },
                        onSearch = { query ->
                            val bundle = bundleOf("searchString" to query)
                            findNavController().navigate(R.id.searchListingFragment, bundle)
                        },
                        onProductClick = { sku, imageUrl ->
                            val bundle = bundleOf(
                                "sku" to sku,
                                "fallbackImageUrl" to imageUrl
                            )
                            findNavController().navigate(R.id.productDetailsFragment, bundle)
                        }
                    )
                }
            }
        }
    }
}
