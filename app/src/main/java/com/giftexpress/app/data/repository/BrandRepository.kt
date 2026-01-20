package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.data.model.ProductListResponse
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class BrandRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getBrands(): NetworkResult<List<BrandResponse>> {
        return try {
            val response = apiService.getBrands()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch brands: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching brands: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun getBrandProducts(
        brandId: Int,
        pageSize: Int,
        currentPage: Int
    ): NetworkResult<ProductListResponse> {
        return try {
            val response = apiService.getBrandProducts(brandId, pageSize, currentPage)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch brand products: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching brand products: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
