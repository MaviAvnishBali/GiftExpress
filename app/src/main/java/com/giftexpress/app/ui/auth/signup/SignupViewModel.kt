package com.giftexpress.app.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.User
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Signup Screen
 * Handles signup logic and form validation
 */
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignupUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<SignupEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val event = _event.asSharedFlow()

    /**
     * Perform signup with validation
     */
    fun signup(firstName: String, lastName: String, dob: String, email: String, password: String, confirmPassword: String) {
        // Validate inputs
        if (firstName.isBlank()) return emitError("First Name is required")

        if (lastName.isBlank()) return emitError("Last Name is required")

        if (dob.isBlank()) return emitError("Date of Birth is required")
        
        if (email.isBlank()) return emitError("Email is required")
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return emitError("Invalid email format")
        
        if (password.isBlank()) return emitError("Password is required")
        
        if (password.length < 6)
            return emitError("Password must be at least 6 characters")
        
        if (password != confirmPassword)
            return emitError("Passwords do not match")

        // Perform signup
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = authRepository.signup(firstName, lastName, dob, email, password)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(SignupEvent.SignupSuccess(result.data!!))
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Signup failed")
                }
                is NetworkResult.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _event.emit(SignupEvent.ShowError(message))
        }
    }
}
