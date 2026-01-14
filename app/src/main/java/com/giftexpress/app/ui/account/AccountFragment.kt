package com.giftexpress.app.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import androidx.compose.ui.platform.ComposeView
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Account Screen Fragment
 * Displays user information and provides logout functionality
 */
@AndroidEntryPoint
class AccountFragment : Fragment() {
    
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                androidx.compose.material3.MaterialTheme() {
                    AccountScreen(
                        viewModel = viewModel,
                        onNavigateToOrders = {
                            findNavController().navigate(R.id.ordersFragment)
                        },
                        onNavigateToWishlist = {
                            // Navigate to wishlist
                        },
                        onNavigateToAddressBook = {
                            // Navigate to address book
                        },
                        onNavigateToAccountInfo = {
                            // Navigate to account info
                        },
                        onNavigateToRewardPoints = {
                            // Navigate to reward points
                        },
                        onNavigateToRewardHistory = {
                            // Navigate to reward history
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLogoutState()
    }

    private fun observeLogoutState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutState.collect { isLoggedOut ->
                    if (isLoggedOut) {
                        showToast("Logged out successfully")
                        findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
                    }
                }
            }
        }
    }
}
