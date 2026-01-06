package com.giftexpress.app.ui.auth.login

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
import com.giftexpress.app.databinding.FragmentLoginBinding
import com.giftexpress.app.data.model.User
import com.giftexpress.app.utils.hide
import com.giftexpress.app.utils.show
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Login Fragment
 * Handles user login UI and interactions
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeLoginState()
    }

    private fun setupListeners() {
        // Back Button
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Login Button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        // Remember Me Checkbox
        binding.cbRememberMe.setOnClickListener {
            viewModel.toggleRememberMe(binding.cbRememberMe.isChecked)
        }

        // Signup Link
        binding.tvSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        // Password Toggle
        var isPasswordVisible = false
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        // Google Sign In (Dummy Implementation for API Testing)
        binding.btnGoogle.setOnClickListener {
            // TODO: Integrate Google Sign-In SDK to get real ID token
            // For now, we simulate a successful Google Sign-In to test the API
            val dummyIdToken = "dummy-google-id-token"
            val dummyEmail = "dheerendra@gmail.com"
            val dummyFirstName = "Dheerendra"
            val dummyLastName = "Singh"
            
            viewModel.googleLogin(dummyIdToken, dummyEmail, dummyFirstName, dummyLastName)
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    /**
     * Observe login state from ViewModel
     */
    private var isAutoFilled = false

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe State (Loading, Remember Me)
                launch {
                    viewModel.state.collect { state ->
                        // Loading State
                        if (state.isLoading) {
                            binding.progressBar.show()
                            binding.btnLogin.isEnabled = false
                        } else {
                            binding.progressBar.hide()
                            binding.btnLogin.isEnabled = true
                        }

                        // Remember Me State
                        binding.cbRememberMe.isChecked = state.rememberMe

                        // Auto-fill credentials once
                        if (!isAutoFilled && state.savedEmail.isNotEmpty()) {
                            binding.etEmail.setText(state.savedEmail)
                            binding.etPassword.setText(state.savedPassword)
                            isAutoFilled = true
                        }
                    }
                }

                // Observe Events (Success, Error)
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is LoginEvent.LoginSuccess -> {
                                showToast("Login Successful")
                                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                            }
                            is LoginEvent.ShowError -> {
                                showToast(event.message)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Enable/disable input fields and button
     */
    private fun enableInputs(enabled: Boolean) {
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.btnLogin.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
