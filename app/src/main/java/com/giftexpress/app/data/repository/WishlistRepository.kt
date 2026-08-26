package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.WishListResponse
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class WishlistRepository @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) {
    suspend fun getWishlist(): NetworkResult<WishListResponse> {
        return try {
            var response = apiService.getWishlist()
            if (response.code() == 401) {
                authRepository.refreshToken()
                response = apiService.getWishlist()
            }
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to load wishlist: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun addToWishlist(sku: String): NetworkResult<Boolean> {
        return try {
            var response = apiService.addToWishlist(sku)
            if (response.code() == 401) {
                authRepository.refreshToken()
                response = apiService.addToWishlist(sku)
            }
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun removeFromWishlist(itemId: Int): NetworkResult<Boolean> {
        return try {
            val response = apiService.removeFromWishlist(itemId)
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Failed: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
