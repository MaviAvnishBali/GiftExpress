package com.giftexpress.app.data.repository

import com.giftexpress.app.data.api.ApiService
import com.giftexpress.app.data.model.RewardHistoryItem
import com.giftexpress.app.data.model.RewardSubscriptionRequest
import com.giftexpress.app.data.model.WalletSummary
import com.giftexpress.app.utils.NetworkResult
import javax.inject.Inject

class RewardRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getRewardPoints(): NetworkResult<WalletSummary> {
        return try {
            val response = apiService.getRewardPoints()
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to load reward points: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun updateSubscription(subscribeEarnings: Boolean, subscribeExpire: Boolean): NetworkResult<Boolean> {
        return try {
            val response = apiService.updateRewardSubscription(
                RewardSubscriptionRequest(subscribeEarnings, subscribeExpire)
            )
            if (response.isSuccessful) NetworkResult.Success(response.body() ?: true)
            else NetworkResult.Error("Failed to update subscription: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }

    suspend fun getRewardHistory(): NetworkResult<List<RewardHistoryItem>> {
        return try {
            val response = apiService.getRewardHistory()
            if (response.isSuccessful && response.body() != null) NetworkResult.Success(response.body()!!)
            else NetworkResult.Error("Failed to load reward history: ${response.message()}")
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.localizedMessage}")
        }
    }
}
