package com.giftexpress.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide holder for the current cart item count, used to drive the header cart badge.
 *
 * A single @Singleton instance is shared across every ViewModel/fragment, so the badge
 * updates the moment an item is added — regardless of which screen triggered the add
 * (home product card, product details, etc.) — and stays correct across navigation.
 *
 * [setCount] is the authoritative sync (called after fetching the real cart);
 * [increment] gives an immediate optimistic bump on add, later reconciled by [setCount].
 */
@Singleton
class CartCountManager @Inject constructor() {

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    /** Authoritative count from the server cart (sum of item quantities). */
    fun setCount(count: Int) {
        _count.value = count.coerceAtLeast(0)
    }

    /** Optimistic bump when an item is added before the cart is re-fetched. */
    fun increment(by: Int = 1) {
        _count.update { (it + by).coerceAtLeast(0) }
    }

    /** Clear on logout. */
    fun reset() {
        _count.value = 0
    }
}
