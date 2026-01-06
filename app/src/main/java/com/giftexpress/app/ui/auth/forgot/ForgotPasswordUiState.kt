package com.giftexpress.app.ui.auth.forgot

/**
 * UI State for Forgot Password Screen
 */
data class ForgotPasswordUiState(
    val isLoading: Boolean = false
)

/**
 * UI Events for Forgot Password Screen
 */
sealed class ForgotPasswordEvent {
    data class Success(val result: Boolean) : ForgotPasswordEvent()
    data class ShowError(val message: String) : ForgotPasswordEvent()
}
