package com.giftexpress.app.ui.brands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.R
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.ui.home.HeroBanner
import com.giftexpress.app.ui.home.HomeHeader
import com.giftexpress.app.ui.home.ProductCard
import com.giftexpress.app.utils.UiState

@Composable
fun BrandProductsScreen(
    brandId: Int,
    brandName: String,
    viewModel: BrandViewModel,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    addedSkus: Set<String>
) {
    val brandProductsState by viewModel.brandProductsState.collectAsState()
    val brandBannersState by viewModel.brandBannersState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    val products = (brandProductsState as? UiState.Success)?.data ?: emptyList()
    val isScrolled = gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
    val shouldLoadMore by remember(products, gridState) {
        derivedStateOf {
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            products.isNotEmpty() && lastVisibleIndex >= products.size - 3
        }
    }

    LaunchedEffect(brandId) {
        viewModel.resetPagination()
        viewModel.fetchBrandBanners(brandId)
        viewModel.fetchBrandProducts(brandId, reset = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Home Header with search
        HomeHeader(
            banners = null,
            onMenuClick = onMenuClick,
            onCartClick = onCartClick,
            isScrolled = isScrolled,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = "Search For 'Perfume'"
        )

        // Content
        when (val state = brandProductsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Error loading products",
                        color = Color.Red
                    )
                }
            }
            is UiState.Success -> {
                val products = state.data
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No products available",
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Banners from API
                        when (val bannerState = brandBannersState) {
                            is UiState.Success -> {
                                val sliders = bannerState.data.sortedBy { it.sequence ?: Int.MAX_VALUE }
                                sliders.forEach { slider ->
                                    when (slider.type) {
                                        "banner" -> {
                                            item(span = { GridItemSpan(3) }) {
                                                slider.banners?.let { banners ->
                                                    HeroBanner(
                                                        banners = banners,
                                                        cornerRadius = 0.dp,
                                                        contentPadding = PaddingValues(0.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }

                        // Products Grid
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCart = { product.sku?.let(onAddToCart) },
                                onGoToCart = onGoToCart,
                                isAdded = product.sku?.let { addedSkus.contains(it) } == true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    LaunchedEffect(shouldLoadMore) {
                        if (shouldLoadMore) {
                            viewModel.loadNextPage()
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

