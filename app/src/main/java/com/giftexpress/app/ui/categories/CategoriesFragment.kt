package com.giftexpress.app.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment() {

    private val viewModel: CategoriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GiftExpressTheme {
                    CategoriesScreen(
                        viewModel = viewModel,
                        onCategoryClick = { categoryId, title ->
                            val bundle = Bundle().apply {
                                putInt("categoryId", categoryId)
                                putString("categoryName", title)
                            }
                            findNavController().navigate(R.id.categoryFragment, bundle)
                        },
                        onSpecialClick = { specialFlag, title, bannerUrl ->
                            val bundle = Bundle().apply {
                                putInt("specialFlag", specialFlag)
                                putString("title", title)
                                bannerUrl?.let { putString("bannerUrl", it) }
                            }
                            findNavController().navigate(R.id.specialProductsFragment, bundle)
                        }
                    )
                }
            }
        }
    }
}
