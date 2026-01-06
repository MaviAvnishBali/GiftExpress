package com.giftexpress.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.User
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Account Screen
 * Manages user profile data and logout functionality
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _changePasswordState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val changePasswordState: StateFlow<UiState<Boolean>> = _changePasswordState

    private val _logoutState = MutableStateFlow(false)
    val logoutState: StateFlow<Boolean> = _logoutState

    init {
        loadUserProfile()
    }

    /**
     * Load user profile from DataStore
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _user.value = user
            }
        }
    }

    /**
     * Change user password
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _changePasswordState.value = UiState.Error("All fields are required")
            return
        }

        if (newPassword.length < 6) {
            _changePasswordState.value = UiState.Error("New password must be at least 6 characters")
            return
        }

        _changePasswordState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.changePassword(currentPassword, newPassword)) {
                is NetworkResult.Success -> {
                    _changePasswordState.value = UiState.Success(result.data ?: false)
                }
                is NetworkResult.Error -> {
                    _changePasswordState.value = UiState.Error(result.message ?: "Change password failed")
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = UiState.Idle
    }

    /**
     * Logout user and clear session
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutState.value = true
        }
    }
}
