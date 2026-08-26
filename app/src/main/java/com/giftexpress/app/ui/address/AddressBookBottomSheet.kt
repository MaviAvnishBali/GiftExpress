package com.giftexpress.app.ui.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.GiftExpressTypography
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressBookBottomSheet : BottomSheetDialogFragment() {

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
                    Box(modifier = Modifier.fillMaxHeight(0.9f)) {
                        AddressBookScreen(
                            viewModel = viewModel,
                            isSelectionMode = isSelectionMode,
                            onBackClick = { dismiss() },
                            onAddAddressClick = {
                                findNavController().navigate(R.id.addressFormFragment)
                                dismiss()
                            },
                            onEditAddressClick = { address ->
                                val bundle = Bundle().apply { putParcelable("address", address) }
                                findNavController().navigate(R.id.addressFormFragment, bundle)
                                dismiss()
                            },
                            onAddressClick = { address ->
                                if (isSelectionMode) {
                                    findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_address", address)
                                    dismiss()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAddresses()
    }
}
