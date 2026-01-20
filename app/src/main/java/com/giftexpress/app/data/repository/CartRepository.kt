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
    private val authRepository: AuthRepository,
    private val dataStore: DataStore<Preferences>
) {
    private val keyQuoteId = stringPreferencesKey(Constants.KEY_QUOTE_ID)

    private suspend fun getAuthToken(): String? {
        return authRepository.getCurrentUser().first()?.token
    }

    private suspend fun getQuoteId(): Int? {
        val savedQuoteId = dataStore.data.map { it[keyQuoteId] }.first()
        if (savedQuoteId != null) return savedQuoteId.toInt()

        val token = getAuthToken() ?: return null
        val response = apiService.createCart("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            val quoteId = response.body()!!
            dataStore.edit { it[keyQuoteId] = quoteId.toString() }
            return quoteId
        }
        return null
    }

    suspend fun getCart(): NetworkResult<CartResponse> {
        return try {
            val token = getAuthToken() ?: return NetworkResult.Error("User not logged in")
            val response = apiService.getCart("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val cartResponse = response.body()!!
                cartResponse.id?.let { quoteId ->
                    dataStore.edit { it[keyQuoteId] = quoteId.toString() }
                }
                NetworkResult.Success(cartResponse)
            } else {
                NetworkResult.Error("Failed to fetch cart: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun addItemToCart(sku: String, qty: Int): NetworkResult<CartItemDetail> {
        return try {
            val token = getAuthToken() ?: return NetworkResult.Error("User not logged in")
            val quoteId = getQuoteId() ?: return NetworkResult.Error("Failed to get quote ID")
            
            val request = AddCartItemRequest(
                cartItem = AddCartItem(sku = sku, qty = qty, quoteId = quoteId)
            )
            val response = apiService.addItemToCart("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to add item: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun updateCartItem(itemId: Int, qty: Int): NetworkResult<CartItemDetail> {
        return try {
            val token = getAuthToken() ?: return NetworkResult.Error("User not logged in")
            val request = UpdateCartItemRequest(
                cartItem = UpdateCartItem(itemId = itemId, qty = qty)
            )
            val response = apiService.updateCartItem("Bearer $token", itemId, request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to update item: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun removeCartItem(itemId: Int): NetworkResult<Boolean> {
        return try {
            val token = getAuthToken() ?: return NetworkResult.Error("User not logged in")
            val response = apiService.removeCartItem("Bearer $token", itemId)
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: true)
            } else {
                NetworkResult.Error("Failed to remove item: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
