package com.giftexpress.app.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.ui.theme.GiftExpressTypography
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private val viewModel: CheckoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = GiftExpressTypography) {
                    CheckoutScreen(
                        viewModel = viewModel,
                        onBackClick = { findNavController().navigateUp() },
                        onChangeAddress = {
                            val bundle = bundleOf("isSelectionMode" to true)
                            findNavController().navigate(R.id.addressBookBottomSheet, bundle)
                        },
                        onProceedToPayment = { total ->
                            val bundle = bundleOf("total" to total.toFloat())
                            findNavController().navigate(R.id.paymentFragment, bundle)
                        },
                        onProductClick = { sku ->
                            val bundle = bundleOf("sku" to sku)
                            findNavController().navigate(R.id.productDetailsFragment, bundle)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Matches iOS viewDidLoad → getAddress() → auto-select default → getShippingMethods → getTotal
        viewModel.loadCheckout()

        // Handle address selected from AddressBookFragment via SavedStateHandle
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<CustomerAddressModel>("selected_address")
            ?.observe(viewLifecycleOwner) { address ->
                address?.let {
                    viewModel.setAddress(it)
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_address", null as CustomerAddressModel?)
                }
            }
    }
}
