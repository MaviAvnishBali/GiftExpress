package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.ProductDetailsResponse
import com.giftexpress.app.data.model.ProductReview
import com.giftexpress.app.data.model.SubmitReviewRequest
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
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

    suspend fun getReviews(sku: String): NetworkResult<List<ProductReview>> {
        return try {
            val response = apiService.getReviews(sku)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to load reviews: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun submitReview(request: SubmitReviewRequest): NetworkResult<Boolean> {
        return try {
            var response = apiService.submitReview(request)
            if (response.code() == 401) {
                authRepository.refreshToken()
                response = apiService.submitReview(request)
            }
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: true)
            } else {
                NetworkResult.Error("Failed to submit review: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
