package com.giftexpress.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.*
import com.giftexpress.app.utils.Constants
import com.giftexpress.app.utils.NetworkResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    private val keyQuoteId = stringPreferencesKey(Constants.KEY_QUOTE_ID)

    suspend fun getOrCreateQuoteId(): Int? = getQuoteId()

    private suspend fun getQuoteId(): Int? {
        val saved = dataStore.data.map { it[keyQuoteId] }.first()
        if (saved != null) return saved.toInt()
        return createQuote()
    }

    /** Creates a fresh Magento quote (POST carts/mine) and persists it. Matches iOS CartViewModel.createCart. */
    private suspend fun createQuote(): Int? {
        val response = apiService.createCart()
        if (response.isSuccessful && response.body() != null) {
            val id = response.body()!!
            dataStore.edit { it[keyQuoteId] = id.toString() }
            return id
        } else if (response.code() == 400 || response.code() == 404) {
            // If active cart already exists (400) or other routing mismatch, try fetching it
            try {
                val existing = apiService.getCart()
                if (existing.isSuccessful && existing.body()?.id != null) {
                    val id = existing.body()!!.id!!
                    dataStore.edit { it[keyQuoteId] = id.toString() }
                    return id
                }
            } catch (e: Exception) {
                // Ignore, we will return null below
            }
        }
        return null
    }

    suspend fun clearQuoteId() {
        dataStore.edit { it.remove(keyQuoteId) }
        createQuote()
    }

    /** Human-readable HTTP error including code + server body, for actionable messages. */
    private fun errorText(response: retrofit2.Response<*>): String {
        val body = try { response.errorBody()?.string() } catch (e: Exception) { null }
        val base = "HTTP ${response.code()} ${response.message()}".trim()
        return if (!body.isNullOrBlank()) "$base — $body" else base
    }

    // Matches iOS fetchCartList: GET carts/mine/items → [CartProduct]
    // An expired (2-hour) token is refreshed transparently by TokenAuthenticator.
    suspend fun getCart(): NetworkResult<List<CartItemDetail>> {
        return try {
            val response = apiService.getCartItems()
            when {
                // Empty cart returns [] (non-null); a null body is also treated as empty.
                response.isSuccessful -> NetworkResult.Success(response.body() ?: emptyList())
                // No active cart yet → create one and show an empty cart (matches iOS 404 → createCart).
                response.code() == 404 -> {
                    createQuote()
                    NetworkResult.Success(emptyList())
                }
                // Session/token not valid (e.g. opening the cart before logging in). Show a
                // friendly, actionable message instead of dumping the raw Magento error/stack trace.
                response.code() == 401 -> NetworkResult.Error("Please log in to view your cart.")
                else -> NetworkResult.Error("Failed to fetch cart: ${errorText(response)}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun addItemToCart(sku: String, qty: Int): NetworkResult<CartItemDetail> {
        return try {
            var quoteId = getQuoteId() ?: return NetworkResult.Error("Failed to get quote ID")
            var response = apiService.addItemToCart(
                AddCartItemRequest(cartItem = AddCartItem(sku = sku, qty = qty, quoteId = quoteId))
            )
            // Self-heal a stale/invalid quote (matches iOS createCart-on-404): recreate and retry once.
            if (!response.isSuccessful && (response.code() == 404 || response.code() == 400)) {
                clearQuoteId()
                quoteId = createQuote() ?: return NetworkResult.Error("Failed to create cart")
                response = apiService.addItemToCart(
                    AddCartItemRequest(cartItem = AddCartItem(sku = sku, qty = qty, quoteId = quoteId))
                )
            }
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to add item: ${errorText(response)}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun updateCartItem(itemId: Int, qty: Int): NetworkResult<CartItemDetail> {
        return try {
            val response = apiService.updateCartItem(
                itemId,
                UpdateCartItemRequest(cartItem = UpdateCartItem(itemId = itemId, qty = qty))
            )
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to update item: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun removeCartItem(itemId: Int): NetworkResult<Boolean> {
        return try {
            val response = apiService.removeCartItem(itemId)
            if (response.isSuccessful) NetworkResult.Success(response.body() ?: true)
            else NetworkResult.Error("Failed to remove item: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun getCartTotals(): NetworkResult<OrderTotals> {
        return try {
            val response = apiService.getCartTotals()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to get totals: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
