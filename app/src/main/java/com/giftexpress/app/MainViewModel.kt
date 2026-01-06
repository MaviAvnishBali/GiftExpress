package com.giftexpress.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.repository.HomeRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _menuState = MutableStateFlow<UiState<List<MenuItem>>>(UiState.Idle)
    val menuState: StateFlow<UiState<List<MenuItem>>> = _menuState

    init {
        fetchMenu()
    }

    fun fetchMenu() {
        _menuState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = homeRepository.getHamburgerMenu()) {
                is NetworkResult.Success -> {
                    _menuState.value = UiState.Success(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    _menuState.value = UiState.Error(result.message ?: "Failed to fetch menu")
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}
