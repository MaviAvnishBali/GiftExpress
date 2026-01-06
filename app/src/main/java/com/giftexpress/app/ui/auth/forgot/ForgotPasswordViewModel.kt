package com.giftexpress.app.ui.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<ForgotPasswordEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val event = _event.asSharedFlow()

    fun forgotPassword(email: String) {
        if (email.isBlank()) return emitError("Email is required")

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return emitError("Invalid email format")

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(email)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.emit(ForgotPasswordEvent.Success(result.data ?: false))
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    emitError(result.message ?: "Request failed")
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _event.emit(ForgotPasswordEvent.ShowError(message))
        }
    }
}
