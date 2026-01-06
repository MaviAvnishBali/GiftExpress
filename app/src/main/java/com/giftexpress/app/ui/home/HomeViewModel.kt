package com.giftexpress.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.model.Post
import com.giftexpress.app.data.model.SliderResponse
import com.giftexpress.app.data.repository.HomeRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * Fetches and manages posts data
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<UiState<List<Post>>>(UiState.Idle)
    val postsState: StateFlow<UiState<List<Post>>> = _postsState

    private val _menuState = MutableStateFlow<UiState<List<MenuItem>>>(UiState.Idle)
    val menuState: StateFlow<UiState<List<MenuItem>>> = _menuState

    private val _slidersState = MutableStateFlow<UiState<List<SliderResponse>>>(UiState.Idle)
    val slidersState: StateFlow<UiState<List<SliderResponse>>> = _slidersState

    init {
        fetchSliders()
        fetchMenu()
    }

    /**
     * Fetch menu items from repository
     */
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
                is NetworkResult.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

    /**
     * Fetch sliders from repository
     */
    fun fetchSliders() {
        _slidersState.value = UiState.Loading
        
        viewModelScope.launch {
            when (val result = homeRepository.getHomeSliders()) {
                is NetworkResult.Success -> {
                    _slidersState.value = UiState.Success(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    _slidersState.value = UiState.Error(result.message ?: "Failed to fetch sliders")
                }
                is NetworkResult.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

}
