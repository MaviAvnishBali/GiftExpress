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
import com.giftexpress.app.databinding.FragmentAccountBinding
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Account Screen Fragment
 * Displays user information and provides logout functionality
 */
@AndroidEntryPoint
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeUserData()
        observeLogoutState()
        setupClickListeners()
    }

    /**
     * Observe user data from ViewModel
     */
    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                    user?.let {
                        binding.tvName.text = it.name
                        binding.tvEmail.text = it.email
                        binding.tvUserId.text = "ID: ${it.id}"
                    }
                }
            }
        }
    }

    /**
     * Observe logout state and navigate on success
     */
    private fun observeLogoutState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutState.collect { isLoggedOut ->
                    if (isLoggedOut) {
                        showToast("Logged out successfully")
                        // Navigate to login screen and clear back stack
                        findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
                    }
                }
            }
        }
    }

    /**
     * Setup click listeners
     */
    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }
        
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
