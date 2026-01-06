package com.giftexpress.app.ui.auth.login

import com.giftexpress.app.data.model.User

/**
 * UI State for Login Screen
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val rememberMe: Boolean = false,
    val savedEmail: String = "",
    val savedPassword: String = ""
)

/**
 * UI Events for Login Screen
 */
sealed class LoginEvent {
    data class LoginSuccess(val user: User) : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}
