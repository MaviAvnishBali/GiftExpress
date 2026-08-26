package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class RewardHistoryItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("action_date") val actionDate: String = "",
    @SerializedName("amount") val amount: Int = 0,
    @SerializedName("comment") val comment: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("points_left") val pointsLeft: Int = 0,
    @SerializedName("customer_id") val customerId: Int = 0,
    @SerializedName("visible_for_customer") val visibleForCustomer: Boolean = true,
    @SerializedName("expiration_date") val expirationDate: String = "",
    @SerializedName("expiring_amount") val expiringAmount: Int = 0
) {
    fun getDisplayDay(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(actionDate) ?: return "--"
            SimpleDateFormat("dd", Locale.getDefault()).format(date)
        } catch (e: Exception) { "--" }
    }

    fun getDisplayMonth(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(actionDate) ?: return "--"
            SimpleDateFormat("MMM yy", Locale.getDefault()).format(date)
        } catch (e: Exception) { "--" }
    }
}
