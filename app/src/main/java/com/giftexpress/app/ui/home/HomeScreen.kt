package com.giftexpress.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.giftexpress.app.utils.UiState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMenuClick: () -> Unit
) {
    val slidersState by viewModel.slidersState.collectAsState()
    val menuState by viewModel.menuState.collectAsState()

    val sliders = (slidersState as? UiState.Success)?.data ?: emptyList()
    val menuItems = (menuState as? UiState.Success)?.data ?: emptyList()

    if (slidersState is UiState.Loading && sliders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 1. Header
            item {
                HomeHeader(onMenuClick = onMenuClick)
            }

            // 2. Dynamic Categories
            if (menuItems.isNotEmpty()) {
                item {
                    CategorySection(categories = menuItems)
                }
            }

            // 3. Dynamic Sections from Sliders
            items(sliders) { slider ->
                when {
                    slider.banners != null -> {
                        HeroBanner(banners = slider.banners)
                        slider.banners.firstOrNull()?.let { banner ->
                            PromoBanner(imageUrl = banner.mobImage)
                        }
                    }
                    slider.products != null -> {
                        ProductSection(
                            title = slider.title ?: "Products",
                            products = slider.products
                        )
                    }
                }
            }

            // 4. Static Brands (Mock for now)
            item {
                BrandSection(brands = getMockBrands())
            }
        }
    }
}

private fun getMockBrands(): List<Brand> {
    return listOf(
        Brand("Armaf"),
        Brand("Azzaro"),
        Brand("Calvin Klein"),
        Brand("Burberry"),
        Brand("Versace")
    )
}
