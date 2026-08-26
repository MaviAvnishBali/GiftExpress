package com.giftexpress.app.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.WishListItem
import com.giftexpress.app.data.model.WishListResponse
import com.giftexpress.app.data.repository.WishlistRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val repository: WishlistRepository
) : ViewModel() {

    private val _wishlistState = MutableStateFlow<UiState<WishListResponse>>(UiState.Idle)
    val wishlistState: StateFlow<UiState<WishListResponse>> = _wishlistState.asStateFlow()

    private val _addToWishlistState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val addToWishlistState: StateFlow<UiState<Boolean>> = _addToWishlistState.asStateFlow()

    private val _removeState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val removeState: StateFlow<UiState<Boolean>> = _removeState.asStateFlow()

    init {
        fetchWishlist()
    }

    fun fetchWishlist() {
        viewModelScope.launch {
            _wishlistState.value = UiState.Loading
            when (val result = repository.getWishlist()) {
                is NetworkResult.Success -> result.data?.let { _wishlistState.value = UiState.Success(it) }
                    ?: run { _wishlistState.value = UiState.Error("Empty response") }
                is NetworkResult.Error -> _wishlistState.value = UiState.Error(result.message ?: "Failed to load wishlist")
                else -> {}
            }
        }
    }

    fun addToWishlist(sku: String) {
        viewModelScope.launch {
            _addToWishlistState.value = UiState.Loading
            when (val result = repository.addToWishlist(sku)) {
                is NetworkResult.Success -> _addToWishlistState.value = UiState.Success(true)
                is NetworkResult.Error -> _addToWishlistState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun removeItem(itemId: Int) {
        viewModelScope.launch {
            _removeState.value = UiState.Loading
            when (val result = repository.removeFromWishlist(itemId)) {
                is NetworkResult.Success -> {
                    _removeState.value = UiState.Success(true)
                    fetchWishlist()
                }
                is NetworkResult.Error -> _removeState.value = UiState.Error(result.message ?: "Failed")
                else -> {}
            }
        }
    }

    fun resetAddState() {
        _addToWishlistState.value = UiState.Idle
    }
}
