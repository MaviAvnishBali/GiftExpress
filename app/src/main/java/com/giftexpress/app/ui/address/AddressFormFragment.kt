package com.giftexpress.app.ui.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.data.model.Address
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressFormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val existingAddress = arguments?.getParcelable<Address>("address")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                    AddressFormScreen(
                        existingAddress = existingAddress,
                        onBackClick = { findNavController().navigateUp() },
                        onSaveAddressClick = { address ->
                            // TODO: Save address via ViewModel
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }
    }
}
