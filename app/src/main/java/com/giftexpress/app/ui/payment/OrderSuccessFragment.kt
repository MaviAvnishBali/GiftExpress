package com.giftexpress.app.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography

class OrderSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val orderId = arguments?.getString("orderId") ?: "N/A"

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    OrderSuccessScreen(
                        orderId = orderId,
                        onContinueShopping = {
                            findNavController().navigate(
                                R.id.homeFragment,
                                null,
                                androidx.navigation.NavOptions.Builder()
                                    .setPopUpTo(R.id.nav_graph, true)
                                    .build()
                            )
                        }
                    )
                }
            }
        }
    }
}
