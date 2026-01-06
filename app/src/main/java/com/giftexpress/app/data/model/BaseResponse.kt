package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Generic Base Response for API
 */
data class BaseResponse<T>(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?
)
