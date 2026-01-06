package com.giftexpress.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Splash Screen
 * Checks login status and determines navigation
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    /**
     * Check login status from DataStore
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            // Add a small delay for splash screen display
            delay(1500)
            
            val loggedIn = authRepository.isLoggedIn()
            _isLoggedIn.value = loggedIn
        }
    }
}
