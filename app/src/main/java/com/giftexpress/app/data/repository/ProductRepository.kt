package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.ProductDetailsResponse
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getProductDetails(sku: String): NetworkResult<ProductDetailsResponse> {
        return try {
            val response = apiService.getProductDetails(sku)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch product details: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
