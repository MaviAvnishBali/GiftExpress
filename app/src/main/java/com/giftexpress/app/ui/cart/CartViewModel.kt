package com.giftexpress.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CartItem
import com.giftexpress.app.data.repository.CartRepository
import com.giftexpress.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchCart()
    }

    fun fetchCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    _cartItems.value = result.data?.items?.map { detail ->
                        CartItem(
                            id = detail.itemId.toString(),
                            name = detail.name ?: "",
                            price = detail.price ?: 0.0,
                            sku = detail.sku ?: "",
                            size = "", // Size might need to be parsed from attributes or SKU
                            quantity = detail.qty ?: 0,
                            image = "" // Image needs to be fetched separately or handled
                        )
                    } ?: emptyList()
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                }
                is NetworkResult.Loading -> {
                    // Already handled by _isLoading
                }
            }
            _isLoading.value = false
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = cartRepository.updateCartItem(itemId.toInt(), newQuantity)) {
                is NetworkResult.Success -> {
                    fetchCart() // Refresh cart after update
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = cartRepository.removeCartItem(itemId.toInt())) {
                is NetworkResult.Success -> {
                    fetchCart() // Refresh cart after removal
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.price * it.quantity }

    val shipping: Double = 0.0

    val tax: Double
        get() = subtotal * 0.136

    val total: Double
        get() = subtotal + shipping + tax
}
