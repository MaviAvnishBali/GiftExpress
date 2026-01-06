package com.giftexpress.app.ui.auth.signup

import com.giftexpress.app.data.model.User

/**
 * UI State for Signup Screen
 */
data class SignupUiState(
    val isLoading: Boolean = false
)

/**
 * UI Events for Signup Screen
 */
sealed class SignupEvent {
    data class SignupSuccess(val user: User) : SignupEvent()
    data class ShowError(val message: String) : SignupEvent()
}
