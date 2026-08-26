package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.OrderApiResponse
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    private fun errorText(response: retrofit2.Response<*>): String {
        val body = try { response.errorBody()?.string() } catch (e: Exception) { null }
        val base = "HTTP ${response.code()} ${response.message()}".trim()
        return if (!body.isNullOrBlank()) "$base — $body" else base
    }

    suspend fun getOrders(): NetworkResult<List<OrderApiResponse>> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to load orders: ${errorText(response)}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun getOrderDetails(orderId: Int): NetworkResult<OrderApiResponse> {
        return try {
            val response = apiService.getOrderDetails(orderId)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to load order details: ${errorText(response)}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
