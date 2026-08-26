package com.giftexpress.app.data.model


import com.google.gson.annotations.SerializedName

typealias CategoriesMainScreenResponse = List<MenuResponse>

data class MenuResponse(
    @SerializedName("menu") val menu: Menu? = null
)

data class Menu(
    @SerializedName("title") val title: String? = null,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("category_image") val categoryImage: String? = null,
    @SerializedName("submenu") val submenu: Submenu? = null
) {
    // Use categoryImage if available, fall back to image — matches iOS categoryImage priority
    val displayImage: String? get() = categoryImage?.takeIf { it.isNotBlank() } ?: image
}

data class Submenu(
    @SerializedName("StatusCode") val statusCode: Int? = null,

    @SerializedName("message") val message: String? = null,

    @SerializedName("data") val sections: List<SubmenuSection>? = null
)

data class SubmenuSection(
    @SerializedName("title") val title: String? = null,

    @SerializedName("type") val type: String? = null, // banner | category

    @SerializedName("sequence") val sequence: Int? = null,

    @SerializedName("banners") val banners: List<Banner>? = null,

    @SerializedName("categories") val categories: List<Category>? = null
)

data class Banner(
    @SerializedName("title") val title: String? = null,

    @SerializedName("mob_image") val mobImage: String? = null
)

data class Category(
    @SerializedName("name") val name: String? = null,

    @SerializedName("image") val image: String? = null,

    @SerializedName("url") val url: String? = null,

    @SerializedName("id") val id: Int? = null
)
