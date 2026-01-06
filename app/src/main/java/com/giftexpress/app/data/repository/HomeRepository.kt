package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.model.Post
import com.giftexpress.app.data.model.SliderResponse
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

/**
 * Repository handling home screen data operations
 * Fetches posts from API
 */
class HomeRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Fetch home sliders from API
     */
    suspend fun getHomeSliders(): NetworkResult<List<SliderResponse>> {
        return try {
            val response = apiService.getHomeSliders()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch sliders: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching sliders: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Fetch hamburger menu items from API
     */
    suspend fun getHamburgerMenu(): NetworkResult<List<MenuItem>> {
        return try {
            val response = apiService.getHamburgerMenu()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch menu: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error fetching menu: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

}
