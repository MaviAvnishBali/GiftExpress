package com.giftexpress.app.ui.home

import com.giftexpress.app.data.model.SliderBanner
import com.giftexpress.app.data.model.SliderProduct

/**
 * Sealed class representing different sections of the Home Screen
 */
sealed class HomeSection {
    object Header : HomeSection()
    data class HeroBanner(val banners: List<SliderBanner>) : HomeSection()
    data class Categories(val categories: List<com.giftexpress.app.data.model.MenuItem>) : HomeSection()
    data class PromoBanner(val imageUrl: String) : HomeSection()
    data class ProductSection(val title: String, val products: List<SliderProduct>) : HomeSection()
    data class Brands(val brands: List<Brand>) : HomeSection()
}


