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
import com.giftexpress.app.databinding.FragmentChangePasswordBinding
import com.giftexpress.app.utils.UiState
import com.giftexpress.app.utils.hide
import com.giftexpress.app.utils.show
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (newPassword != confirmPassword) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }

            viewModel.changePassword(currentPassword, newPassword)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changePasswordState.collect { state ->
                    when (state) {
                        is UiState.Idle -> {
                            binding.progressBar.hide()
                            binding.btnSubmit.isEnabled = true
                        }
                        is UiState.Loading -> {
                            binding.progressBar.show()
                            binding.btnSubmit.isEnabled = false
                        }
                        is UiState.Success -> {
                            binding.progressBar.hide()
                            binding.btnSubmit.isEnabled = true
                            showToast("Password changed successfully")
                            viewModel.resetChangePasswordState()
                            findNavController().navigateUp()
                        }
                        is UiState.Error -> {
                            binding.progressBar.hide()
                            binding.btnSubmit.isEnabled = true
                            showToast(state.message)
                            viewModel.resetChangePasswordState()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
