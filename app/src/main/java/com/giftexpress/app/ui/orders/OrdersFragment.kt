package com.giftexpress.app.ui.orders

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
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private val viewModel: OrdersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadOrders()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrders()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    OrdersScreen(
                        viewModel = viewModel,
                        onBackClick = { findNavController().navigateUp() },
                        onOrderClick = { orderId ->
                            val action = OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment(orderId.toString())
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }
}
