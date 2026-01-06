package com.giftexpress.app.ui.auth.login

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
 * ViewModel for Login Screen
 * Handles login logic and form validation
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<LoginEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val event = _event.asSharedFlow()

    init {
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        viewModelScope.launch {
            // Load Remember Me status
            authRepository.isRememberMeEnabled().collect { enabled ->
                _state.update { it.copy(rememberMe = enabled) }
            }
        }
        viewModelScope.launch {
            // Load Saved Credentials
            authRepository.getSavedCredentials().collect { credentials ->
                _state.update { 
                    it.copy(
                        savedEmail = credentials.first,
                        savedPassword = credentials.second
                    ) 
                }
            }
        }
    }

    /**
     * Toggle Remember Me status
     */
    fun toggleRememberMe(enabled: Boolean) {
        _state.update { it.copy(rememberMe = enabled) }
    }

    /**
     * Perform Google Login
     */
    fun googleLogin(idToken: String, email: String, firstName: String, lastName: String) {
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = authRepository.googleLogin(idToken, email, firstName, lastName)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    // For Google login, we don't usually save password, but we can save the email if needed
                    // However, the requirement says "username and password", which usually applies to email login
                    _event.emit(LoginEvent.LoginSuccess(result.data!!))
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Google Login failed")
                }
                is NetworkResult.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

    /**
     * Perform login with validation
     */
    fun login(email: String, password: String) {
        // Validate inputs
        if (email.isBlank()) return emitError("Email is required")
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return emitError("Invalid email format")
        
        if (password.isBlank()) return emitError("Password is required")
        
        if (password.length < 6)
            return emitError("Password must be at least 6 characters")

        // Perform login
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    
                    // Save credentials if Remember Me is enabled
                    authRepository.saveRememberMeCredentials(
                        email = email,
                        password = password,
                        enabled = _state.value.rememberMe
                    )
                    
                    _event.emit(LoginEvent.LoginSuccess(result.data!!))
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Login failed")
                }
                is NetworkResult.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _event.emit(LoginEvent.ShowError(message))
        }
    }
}
