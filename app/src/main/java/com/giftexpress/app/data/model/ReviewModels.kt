package com.giftexpress.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductReview(
    @SerializedName("title") val title: String = "",
    @SerializedName("detail") val detail: String = "",
    @SerializedName("nickname") val nickname: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("ratings") val ratings: List<ReviewRating> = emptyList()
) {
    fun averageRating(): Float {
        if (ratings.isEmpty()) return 0f
        return ratings.mapNotNull { it.percent?.toFloat() }.average().toFloat() / 20f
    }
}

data class ReviewRating(
    @SerializedName("label") val label: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("percent") val percent: Int? = null
)

// Magento expects the payload wrapped in a "review" object; the flat form returns
// 400 ("review" is required). Matches iOS: rating_id "2" and string values.
data class SubmitReviewRequest(
    @SerializedName("review") val review: ReviewBody
)

data class ReviewBody(
    @SerializedName("sku") val sku: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("title") val title: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("ratings") val ratings: List<ReviewRatingInput>
)

data class ReviewRatingInput(
    @SerializedName("rating_id") val ratingId: String = "2",
    @SerializedName("value") val value: String
)
