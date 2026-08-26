package com.giftexpress.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giftexpress.app.data.model.CartItem
import com.giftexpress.app.data.model.CartItemDetail
import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.data.repository.CartCountManager
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
    private val cartRepository: CartRepository,
    private val cartCountManager: CartCountManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addedSkus = MutableStateFlow<Set<String>>(emptySet())
    val addedSkus: StateFlow<Set<String>> = _addedSkus.asStateFlow()

    private val _cartEvents = kotlinx.coroutines.flow.MutableSharedFlow<CartEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val cartEvents: kotlinx.coroutines.flow.SharedFlow<CartEvent> = _cartEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            cartCountManager.count.collect { count ->
                if (count == 0) {
                    _cartItems.value = emptyList()
                    _addedSkus.value = emptySet()
                }
            }
        }
    }

    fun fetchCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    val items: List<CartItemDetail> = result.data ?: emptyList()
                    _cartItems.value = items.map { detail ->
                        CartItem(
                            id = detail.itemId.toString(),
                            name = detail.name ?: "",
                            price = detail.price ?: 0.0,
                            sku = detail.sku ?: "",
                            size = detail.size ?: "",      // extension_attributes.available_sizes
                            quantity = detail.qty ?: 0,
                            image = detail.image ?: ""     // extension_attributes.product_image
                        )
                    }
                    _addedSkus.value = items.mapNotNull { it.sku }.toSet()
                    // Authoritative sync of the header cart badge with the real cart.
                    cartCountManager.setCount(items.sumOf { it.qty ?: 0 })
                    fetchCartTotals()
                }
                is NetworkResult.Error -> {
                    _error.value = result.message
                    if (result.message?.contains("login", ignoreCase = true) == true ||
                        result.message?.contains("log in", ignoreCase = true) == true
                    ) {
                        _cartItems.value = emptyList()
                        _addedSkus.value = emptySet()
                        cartCountManager.setCount(0)
                    }
                }
                is NetworkResult.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    /**
     * Silently refresh just the header cart badge from the server cart.
     * Used by the Home screen: no loading spinner, no error toast — a failure (e.g. a
     * logged-out 401) simply leaves the count untouched. Skips the network call entirely
     * when the user isn't logged in (there is no `carts/mine` for a guest).
     */
    fun syncCartCount() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                return@launch
            }
            when (val result = cartRepository.getCart()) {
                is NetworkResult.Success -> {
                    val items = result.data ?: emptyList()
                    val serverSkus = items.mapNotNull { it.sku }.toSet()
                    cartCountManager.setCount(items.sumOf { it.qty ?: 0 })
                    _addedSkus.update { current -> current + serverSkus }
                }
                else -> { /* silent — never surface cart errors on the Home screen */ }
            }
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
                    cartCountManager.increment(qty)   // instant optimistic bump
                    _cartEvents.emit(CartEvent.ItemAdded(sku))
                    syncCartCount()                   // then reconcile to the real server count
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

    fun clearCart() {
        _cartItems.value = emptyList()
        _addedSkus.value = emptySet()
        cartCountManager.setCount(0)
    }

    private val _subtotal = MutableStateFlow(0.0)
    val subtotal: StateFlow<Double> = _subtotal.asStateFlow()

    private val _shipping = MutableStateFlow(0.0)
    val shipping: StateFlow<Double> = _shipping.asStateFlow()

    private val _tax = MutableStateFlow(0.0)
    val tax: StateFlow<Double> = _tax.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total.asStateFlow()

    fun fetchCartTotals() {
        viewModelScope.launch {
            when (val result = cartRepository.getCartTotals()) {
                is NetworkResult.Success -> {
                    result.data?.let { totals ->
                        _subtotal.value = totals.subtotal ?: 0.0
                        _shipping.value = totals.shippingAmount ?: 0.0
                        _tax.value = totals.taxAmount ?: 0.0
                        _discount.value = totals.discountAmount ?: 0.0
                        _total.value = totals.grandTotal ?: 0.0
                    }
                }
                else -> {}
            }
        }
    }
        
    fun isLoggedIn(): Boolean = authRepository.isLoggedInSync()
    fun setPendingCartSku(sku: String) { authRepository.pendingCartSku = sku }
    fun setPendingWishlistSku(sku: String) { authRepository.pendingWishlistSku = sku }
}

sealed class CartEvent {
    data class ItemAdded(val sku: String) : CartEvent()
}
