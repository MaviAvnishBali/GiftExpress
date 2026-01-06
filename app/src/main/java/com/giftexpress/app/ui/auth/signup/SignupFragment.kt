package com.giftexpress.app.ui.auth.signup

import android.app.DatePickerDialog
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
import com.giftexpress.app.databinding.FragmentSignupBinding
import com.giftexpress.app.data.model.User
import com.giftexpress.app.utils.hide
import com.giftexpress.app.utils.show
import com.giftexpress.app.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * Signup Fragment
 * Handles user registration UI and interactions
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
        
        setupClickListeners()
        observeSignupState()
    }

    /**
     * Setup click listeners for buttons
     */
    /**
     * Setup click listeners for buttons
     */
    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val dob = binding.etDOB.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            viewModel.signup(firstName, lastName, dob, email, password, confirmPassword)
        }
        
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
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

        // Confirm Password Toggle
        var isConfirmPasswordVisible = false
        binding.ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                binding.etConfirmPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                binding.etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }

        binding.whydob.setOnClickListener {
            showToast("We need your DOB to verify your age.")
        }

        // DOB Date Picker
        binding.ivDOB.setOnClickListener {
            showDatePicker()
        }
    }

    /**
     * Show Date Picker Dialog
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val formattedDate = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )

                binding.etDOB.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    /**
     * Observe signup state from ViewModel
     */
    private fun observeSignupState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe State (Loading)
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

                // Observe Events (Success, Error)
                launch {
                    viewModel.event.collect { event ->
                        when (event) {
                            is SignupEvent.SignupSuccess -> {
                                showToast("Signup successful!")
                                findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
                            }
                            is SignupEvent.ShowError -> {
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
        binding.etFirstName.isEnabled = enabled
        binding.etLastName.isEnabled = enabled
        binding.etDOB.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.etConfirmPassword.isEnabled = enabled
        binding.btnSignup.isEnabled = enabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
