package com.giftexpress.app.ui.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.core.widget.doOnTextChanged

/**
 * Login Fragment
 * Handles user login UI and interactions
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()
    private val cartViewModel: com.giftexpress.app.ui.cart.CartViewModel by activityViewModels()
    private val wishlistViewModel: com.giftexpress.app.ui.wishlist.WishlistViewModel by viewModels()
    
    private lateinit var googleSignInClient: GoogleSignInClient
    
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            // socialId = account.id (Google user ID), matches what iOS sends as user.userID
            val socialId = account?.id ?: ""
            val email = account?.email ?: ""

            if (email.isBlank() || socialId.isBlank()) {
                showToast("Google Sign-In failed: could not get account info")
                return@registerForActivityResult
            }

            val givenName = account?.givenName ?: ""
            val familyName = account?.familyName ?: ""
            val displayName = account?.displayName ?: ""

            var firstName = givenName
            var lastName = familyName
            if (firstName.isEmpty() && displayName.isNotEmpty()) {
                val parts = displayName.split(" ")
                firstName = parts.firstOrNull() ?: ""
                lastName = parts.drop(1).joinToString(" ")
            }
            val finalFirstName = firstName.ifBlank { email.substringBefore("@") }
            val finalLastName = lastName.ifBlank { "User" }

            viewModel.googleLogin(email, finalFirstName, finalLastName, socialId)
        } catch (e: ApiException) {
            Log.e("LoginFragment", "Google Sign-In failed", e)
            showToast("Google Sign-In failed: ${e.statusCode}")
        }
    }

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
        
        // Initialize Google Sign-In Client
        // We only need email + user ID (socialId), matching iOS which uses user.userID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        
        // Hide back button when login is the root screen (navigated from splash)
        val hasPreviousScreen = findNavController().previousBackStackEntry != null
        binding.ivBack.visibility = if (hasPreviousScreen) View.VISIBLE else View.GONE

        setupListeners()
        observeLoginState()
    }

    private var isLoginLoading = false

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

        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            updateLoginButtonState()
        }

        binding.etPassword.doOnTextChanged { _, _, _, _ ->
            updateLoginButtonState()
        }

        // Remember Me Checkbox
        binding.cbRememberMe.setOnClickListener {
            viewModel.toggleRememberMe(binding.cbRememberMe.isChecked)
        }

        // Signup Link
        binding.tvSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        // Password Toggle — preserve autofillHints by using transformationMethod instead of inputType
        var isPasswordVisible = false
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.transformationMethod = if (isPasswordVisible)
                android.text.method.HideReturnsTransformationMethod.getInstance()
            else
                android.text.method.PasswordTransformationMethod.getInstance()
            binding.ivPasswordToggle.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        // Google Sign In
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
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
                        isLoginLoading = state.isLoading
                        if (state.isLoading) {
                            binding.progressBar.show()
                        } else {
                            binding.progressBar.hide()
                        }
                        updateLoginButtonState()

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
                                
                                val pendingCart = viewModel.getPendingCartSku()
                                val pendingWishlist = viewModel.getPendingWishlistSku()
                                
                                if (pendingCart != null) {
                                    cartViewModel.addProductToCart(pendingCart)
                                    viewModel.clearPendingCartSku()
                                }
                                if (pendingWishlist != null) {
                                    wishlistViewModel.addToWishlist(pendingWishlist)
                                    viewModel.clearPendingWishlistSku()
                                }

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
        applyLoginButtonState(enabled && areInputsFilled())
    }

    private fun areInputsFilled(): Boolean {
        return binding.etEmail.text?.toString()?.isNotBlank() == true &&
            binding.etPassword.text?.toString()?.isNotBlank() == true
    }

    private fun updateLoginButtonState() {
        val shouldEnable = !isLoginLoading && areInputsFilled()
        applyLoginButtonState(shouldEnable)
    }

    private fun applyLoginButtonState(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        binding.btnLogin.setBackgroundResource(
            if (enabled) R.drawable.bg_button_dark else R.drawable.bg_button_disabled
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
}
