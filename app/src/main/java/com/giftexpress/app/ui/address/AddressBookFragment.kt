package com.giftexpress.app.ui.address

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
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressBookFragment : Fragment() {

    private val viewModel: AddressBookViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val isSelectionMode = arguments?.getBoolean("isSelectionMode", false) ?: false

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    AddressBookScreen(
                        viewModel = viewModel,
                        isSelectionMode = isSelectionMode,
                        onBackClick = { findNavController().navigateUp() },
                        onAddAddressClick = {
                            findNavController().navigate(R.id.addressFormFragment)
                        },
                        onEditAddressClick = { address ->
                            val bundle = Bundle().apply { putParcelable("customerAddress", address) }
                            findNavController().navigate(R.id.addressFormFragment, bundle)
                        },
                        onAddressClick = { address ->
                            if (isSelectionMode) {
                                findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_address", address)
                                findNavController().navigateUp()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Matches iOS viewWillAppear → getAddress
        viewModel.loadAddresses()
    }
}
