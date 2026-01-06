package com.giftexpress.app.ui.auth.forgot

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
import com.giftexpress.app.databinding.FragmentForgotPasswordBinding
import com.giftexpress.app.utils.hide
import com.giftexpress.app.utils.show
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
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
            val email = binding.etEmail.text.toString()
            viewModel.forgotPassword(email)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe State (Loading)
                launch {
                    viewModel.state.collect { state ->
                        if (state.isLoading) {
                            binding.progressBar.show()
                            binding.btnSubmit.isEnabled = false
                        } else {
                            binding.progressBar.hide()
                            binding.btnSubmit.isEnabled = true
                        }
                    }
                }

                // Observe Events (Success, Error)
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ForgotPasswordEvent.Success -> {
                                showToast("Reset link sent to your email")
                                findNavController().navigateUp()
                            }
                            is ForgotPasswordEvent.ShowError -> {
                                showToast(event.message)
                            }
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
