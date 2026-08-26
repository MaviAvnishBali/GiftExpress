package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class WalletSummary(
    @SerializedName("balance") val balance: Int = 0,
    @SerializedName("total_earned") val totalEarned: Int = 0,
    @SerializedName("total_spent") val totalSpent: Int = 0,
    @SerializedName("expired") val expired: Int = 0,
    @SerializedName("subscribe_earnings") val subscribeEarnings: Boolean = false,
    @SerializedName("subscribe_expire") val subscribeExpire: Boolean = false
)

data class RewardSubscriptionRequest(
    @SerializedName("subscribe_earnings") val subscribeEarnings: Boolean,
    @SerializedName("subscribe_expire") val subscribeExpire: Boolean
)
