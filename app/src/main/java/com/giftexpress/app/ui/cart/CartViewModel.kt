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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
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

    private val _addedSkus = MutableStateFlow<Set<String>>(emptySet())
    val addedSkus: StateFlow<Set<String>> = _addedSkus.asStateFlow()

    private val _cartEvents = kotlinx.coroutines.flow.MutableSharedFlow<CartEvent>()
    val cartEvents: kotlinx.coroutines.flow.SharedFlow<CartEvent> = _cartEvents.asSharedFlow()

    init {
        fetchCart()
    }

    fun fetchCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    val items = result.data?.items ?: emptyList()
                    _cartItems.value = items.map { detail ->
                        CartItem(
                            id = detail.itemId.toString(),
                            name = detail.name ?: "",
                            price = detail.price ?: 0.0,
                            sku = detail.sku ?: "",
                            size = detail.attributes?.size ?: "",
                            quantity = detail.qty ?: 0,
                            image = detail.image ?: ""
                        )
                    }
                    items
                        .mapNotNull { it.sku }
                        .let { skus ->
                            if (skus.isNotEmpty()) {
                                _addedSkus.update { current -> current + skus }
                            }
                        }
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

    fun addProductToCart(sku: String, qty: Int = 1) {
        viewModelScope.launch {
            when (val result = cartRepository.addItemToCart(sku, qty)) {
                is NetworkResult.Success -> {
                    _addedSkus.update { it + sku }
                    _cartEvents.emit(CartEvent.ItemAdded(sku))
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                }
                is NetworkResult.Loading -> {
                    _isLoading.value = true
                }
            }
            _isLoading.value = false
        }
    }

    fun notifyItemAdded(sku: String) {
        if (sku.isNotBlank()) {
            _addedSkus.update { it + sku }
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

sealed class CartEvent {
    data class ItemAdded(val sku: String) : CartEvent()
}
