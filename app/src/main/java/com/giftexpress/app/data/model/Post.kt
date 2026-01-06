package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a Post
 * Used in home screen RecyclerView
 */
data class Post(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("body")
    val body: String
)
