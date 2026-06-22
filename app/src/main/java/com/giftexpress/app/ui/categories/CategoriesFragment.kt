package com.giftexpress.app.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.giftexpress.app.databinding.FragmentCategoriesBinding
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.giftexpress.app.ui.theme.GiftExpressTheme

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private val viewModel: CategoriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GiftExpressTheme {
                    CategoriesScreen(viewModel = viewModel)
                }
            }
        }
    }
}
