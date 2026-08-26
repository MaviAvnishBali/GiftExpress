package com.giftexpress.app.ui.auth.signup

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.giftexpress.app.R
import com.giftexpress.app.databinding.FragmentSignupBinding
import com.giftexpress.app.utils.hide
import com.giftexpress.app.utils.show
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Signup Fragment — matches iOS SignUpViewController
 * Fields: First Name, Last Name, DOB (yyyy-MM-dd), Email, Password, Confirm Password, Newsletter
 * After signup: auto-logs in and navigates to home (same as iOS callLoginAPI)
 */
@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // DOB field: read-only, only openable via date picker (matches iOS inputView = dobPicker)
        binding.etDOB.isFocusable = false
        binding.etDOB.isFocusableInTouchMode = false
        binding.etDOB.setOnClickListener { showDatePicker() }
        setupClickListeners()
        observeSignupState()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val dob = binding.etDOB.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val isSubscribed = binding.signupfornewsleter.isChecked

            viewModel.signup(firstName, lastName, dob, email, password, confirmPassword, isSubscribed)
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Password toggle
        var isPasswordVisible = false
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.inputType = if (isPasswordVisible)
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivPasswordToggle.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        // Confirm password toggle
        var isConfirmPasswordVisible = false
        binding.ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            binding.etConfirmPassword.inputType = if (isConfirmPasswordVisible)
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivConfirmPasswordToggle.setImageResource(
                if (isConfirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }

        binding.whydob.setOnClickListener {
            showToast("We need your DOB to verify your age.")
        }

        // Calendar icon opens date picker
        binding.ivDOB.setOnClickListener { showDatePicker() }
    }

    /**
     * DOB picker — stores yyyy-MM-dd format matching iOS (formatter.dateFormat = "yyyy-MM-dd")
     * No future dates allowed (matches iOS dobPicker.maximumDate = Date())
     */
    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                // API format: yyyy-MM-dd (same as iOS)
                binding.etDOB.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis() // no future dates
            show()
        }
    }

    private fun observeSignupState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        if (state.isLoading) {
                            binding.progressBar.show()
                            enableInputs(false)
                        } else {
                            binding.progressBar.hide()
                            enableInputs(true)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is SignupEvent.SignupSuccess -> {
                                showToast("Login successful 🎉")
                                findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
                            }
                            is SignupEvent.ShowError -> showToast(event.message)
                            is SignupEvent.DuplicateEmail -> showDuplicateEmailDialog(event.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * Enable/disable input fields and button
     */
    private fun showDuplicateEmailDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Account Exists")
            .setMessage(message)
            .setPositiveButton("Login") { _, _ ->
                findNavController().navigateUp() // go back to Login
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun enableInputs(enabled: Boolean) {
        binding.etFirstName.isEnabled = enabled
        binding.etLastName.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.etConfirmPassword.isEnabled = enabled
        binding.signupfornewsleter.isEnabled = enabled
        binding.btnSignup.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
