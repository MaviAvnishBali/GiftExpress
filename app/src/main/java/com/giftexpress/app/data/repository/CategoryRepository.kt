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
        manufacturer: Int? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        specialFlag: Int? = null,
        filters: Map<String, String> = emptyMap()
    ): NetworkResult<CategoryProductsResponse> {
        return try {
            val response = if (categoryId == 15) {
                apiService.getSpecialProducts(
                    specialFlag = 15,
                    pageSize = pageSize,
                    currentPage = currentPage,
                    sortBy = sortBy,
                    sortOrder = sortOrder,
                    manufacturer = manufacturer,
                    filters = filters
                )
            } else {
                apiService.getCategoryProducts(
                    categoryId,
                    pageSize,
                    currentPage,
                    manufacturer,
                    sortBy,
                    sortOrder,
                    specialFlag,
                    filters
                )
            }
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else if (response.code() == 404) {
                NetworkResult.Success(CategoryProductsResponse(items = emptyList()))
            } else {
                NetworkResult.Error("Failed to fetch products: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching products: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun getSpecialProducts(
        specialFlag: Int,
        pageSize: Int,
        currentPage: Int,
        sortBy: String? = null,
        sortOrder: String? = null,
        manufacturer: Int? = null,
        filters: Map<String, String> = emptyMap()
    ): NetworkResult<CategoryProductsResponse> {
        return try {
            val response = apiService.getSpecialProducts(specialFlag, pageSize, currentPage, sortBy, sortOrder, manufacturer, filters)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else if (response.code() == 404) {
                NetworkResult.Success(CategoryProductsResponse(items = emptyList()))
            } else {
                NetworkResult.Error("Failed to fetch products: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun getCategoriesMainScreen(): NetworkResult<com.giftexpress.app.data.model.CategoriesMainScreenResponse> {
        return try {
            val response = apiService.getCategoriesMainScreen()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch categories: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching categories: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
