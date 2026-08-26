package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Actual login API response from POST integration/customer/token and POST giftexpress/social-login
 * Returns JSON object: {"access_token":"...","refresh_token":"...","customer_id":123}
 */
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("customer_id") val customerId: Int? = null
)

/**
 * Body for POST giftexpress/auth/refresh.
 * Matches iOS: { "refreshToken": "<refresh_token>" }.
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * Legacy AuthResponse — kept for compatibility
 */
data class AuthResponse(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("userId") val userId: Int? = null,
    val token: String? = null,
    val message: String? = null
)
