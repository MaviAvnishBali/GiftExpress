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
import com.giftexpress.app.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressBookFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = com.giftexpress.app.ui.theme.GiftExpressTypography) {
                    AddressBookScreen(
                        onBackClick = { findNavController().navigateUp() },
                        onAddAddressClick = {
                            findNavController().navigate(R.id.addressFormFragment)
                        },
                        onEditAddressClick = { address ->
                            val bundle = Bundle().apply { putParcelable("address", address) }
                            findNavController().navigate(R.id.addressFormFragment, bundle)
                        },
                        onDeleteAddressClick = { address ->
                            // TODO: Implement delete logic
                        }
                    )
                }
            }
        }
    }
}
