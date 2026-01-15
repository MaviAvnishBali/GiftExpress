package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.CategoryProductsResponse
import com.giftexpress.app.data.model.SliderResponse
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCategoryOfferAndBanner(categoryId: Int): NetworkResult<List<SliderResponse>> {
        return try {
            val response = apiService.getCategoryOfferAndBanner(categoryId)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.sliders)
            } else {
                NetworkResult.Error("Failed to fetch category data: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching category data: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun getCategoryProducts(
        categoryId: Int,
        pageSize: Int,
        currentPage: Int,
        manufacturer: Int? = null
    ): NetworkResult<CategoryProductsResponse> {
        return try {
            val response = apiService.getCategoryProducts(categoryId, pageSize, currentPage, manufacturer)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch products: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching products: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
