package com.giftexpress.app.ui.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.ui.theme.GiftExpressTypography
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddressFormFragment : Fragment() {

    private val viewModel: AddressBookViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val existingAddress = arguments?.getParcelable<CustomerAddressModel>("customerAddress")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    AddressFormScreen(
                        existingAddress = existingAddress,
                        viewModel = viewModel,
                        onBackClick = { findNavController().navigateUp() },
                        onSaved = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is UiState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetSaveState()
                        }
                        is UiState.Success -> {
                            Toast.makeText(context, "Address saved successfully", Toast.LENGTH_SHORT).show()
                            viewModel.resetSaveState()
                            findNavController().navigateUp()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
