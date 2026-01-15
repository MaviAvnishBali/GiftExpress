package com.giftexpress.app.ui.cart

import androidx.lifecycle.ViewModel
import com.giftexpress.app.data.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        // Load dummy data
        _cartItems.value = listOf(
            CartItem(
                id = "1",
                name = "Ariana Grande Moonlight",
                price = 22.95,
                sku = "3.4 oz (10 ML)",
                size = "3.4 oz",
                quantity = 1,
                image = "https://www.giftexpress.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/a/r/ariana_grande_moonlight_3.4_oz_edp_spray_for_women.jpg"
            ),
            CartItem(
                id = "2",
                name = "Ariana Grande Moonlight",
                price = 22.95,
                sku = "3.4 oz (10 ML)",
                size = "3.4 oz",
                quantity = 1,
                image = "https://www.giftexpress.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/a/r/ariana_grande_moonlight_3.4_oz_edp_spray_for_women.jpg"
            ),
            CartItem(
                id = "3",
                name = "Ariana Grande Moonlight",
                price = 22.95,
                sku = "3.4 oz (10 ML)",
                size = "3.4 oz",
                quantity = 1,
                image = "https://www.giftexpress.com/media/catalog/product/cache/1/image/9df78eab33525d08d6e5fb8d27136e95/a/r/ariana_grande_moonlight_3.4_oz_edp_spray_for_women.jpg"
            )
        )
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        _cartItems.value = _cartItems.value.map {
            if (it.id == itemId) it.copy(quantity = newQuantity) else it
        }
    }

    fun removeItem(itemId: String) {
        _cartItems.value = _cartItems.value.filter { it.id != itemId }
    }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.price * it.quantity }

    val shipping: Double = 0.0

    val tax: Double
        get() = subtotal * 0.136 // Roughly matching the image's 9.39 / 68.85

    val total: Double
        get() = subtotal + shipping + tax
}
