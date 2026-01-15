package com.giftexpress.app.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.compose.material3.MaterialTheme
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private val viewModel: CartViewModel by viewModels()

    override fun onCreateView(
        LayoutInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    CartScreen(
                        viewModel = viewModel,
                        onBackClick = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }
}
