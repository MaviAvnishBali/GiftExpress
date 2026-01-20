package com.giftexpress.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.utils.UiState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMenuClick: () -> Unit,
    onCategoryClick: (SliderProduct) -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    addedSkus: Set<String>,
    onCartClick: () -> Unit
) {
    val slidersState by viewModel.slidersState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0

    val sliders = (slidersState as? UiState.Success)?.data?.sortedBy { it.sequence ?: Int.MAX_VALUE } ?: emptyList()

    if (slidersState is UiState.Loading && sliders.isEmpty()) {
        HomeShimmerLoading()
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Static Top Bar and Search Bar (Fixed & Animated)
            HomeHeader(
                banners = null,
                onMenuClick = onMenuClick,
                onCartClick = onCartClick,
                isScrolled = isScrolled
            )

            // 2. Dynamic Sections based on Sequence (Scrollable)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(sliders) { slider ->
                    when (slider.type) {
                        "banner" -> {
                            slider.banners?.let { banners ->
                                HeroBanner(
                                    banners = banners,
                                    cornerRadius = 0.dp,
                                    contentPadding = PaddingValues(0.dp)
                                )
                            }
                        }

                        "category" -> {
                            slider.products?.let { products ->
                                CategorySlider(
                                    title = slider.title ?: "Shop By Category",
                                    categories = products,
                                    onCategoryClick = onCategoryClick
                                )
                            }
                        }

                        "offer" -> {
                            slider.offers?.let { offers ->
                                OffersSection(
                                    title = slider.title ?: "Offers",
                                    offers = offers
                                )
                            }
                        }

                        "slider" -> {
                            slider.products?.let { products ->
                                ProductSection(
                                    title = slider.title ?: "Products",
                                    products = products,
                                    onProductClick = onProductClick,
                                    onAddToCart = onAddToCart,
                                    addedSkus = addedSkus,
                                    onGoToCart = onGoToCart
                                )
                            }
                        }

                        "brand" -> {
                            slider.products?.let { products ->
                                BrandSection(
                                    title = slider.title ?: "Popular Brands",
                                    brands = products
                                )
                            }
                        }

                        // Fallback for older structure or missing type
                        else -> {
                            if (slider.type == null) {
                                when {
                                    slider.banners != null -> {
                                        HeroBanner(banners = slider.banners)
                                        slider.banners.firstOrNull()?.let { banner ->
                                            PromoBanner(imageUrl = banner.mobImage ?: "")
                                        }
                                    }

                                    slider.products != null -> {
                                        val firstProduct = slider.products.firstOrNull()
                                        when {
                                            firstProduct?.price == null && firstProduct?.name == null && firstProduct?.url != null -> {
                                                BrandSection(
                                                    title = slider.title ?: "Popular Brands",
                                                    brands = slider.products
                                                )
                                            }

                                            firstProduct?.id != null -> {
                                                CategorySlider(
                                                    title = slider.title ?: "Shop By Category",
                                                    categories = slider.products,
                                                    onCategoryClick = { product ->
                                                        product.sku?.let(onProductClick)
                                                    }
                                                )
                                            }

                                            else -> {
                                                ProductSection(
                                                    title = slider.title ?: "Products",
                                                    products = slider.products,
                                                    onProductClick = onProductClick,
                                                    onAddToCart = onAddToCart,
                                                    addedSkus = addedSkus,
                                                    onGoToCart = onGoToCart
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeShimmerLoading() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header Shimmer (Fixed)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shimmerEffect()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Category Shimmer
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .width(150.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(5) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .shimmerEffect()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }
                }
            }

            // Offers Shimmer
            item {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .width(100.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerEffect()
                    )
                }
            }

            // Product Section Shimmer
            items(2) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(3) {
                            ProductCardShimmer()
                        }
                    }
                }
            }
        }
    }
}
