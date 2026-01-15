package com.giftexpress.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.home.*
import com.giftexpress.app.utils.UiState

@Composable
fun CategoryScreen(
    categoryId: Int,
    categoryName: String,
    viewModel: CategoryViewModel,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit
) {
    val categoryDataState by viewModel.categoryDataState.collectAsState()
    val productsState by viewModel.productsState.collectAsState()

    Scaffold(
        topBar = {
            CategoryTopBar(title = categoryName, onBackClick = onBackClick)
        },
        bottomBar = {
            CategoryBottomActions()
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1. Category Data (Banner, Categories, Brands)
                when (val state = categoryDataState) {
                    is UiState.Success -> {
                        val sliders = state.data.sortedBy { it.sequence ?: Int.MAX_VALUE }
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
                                "category" -> {
                                    item(span = { GridItemSpan(3) }) {
                                        if (slider.title?.contains("Popular", ignoreCase = true) == true) {
                                            BrandSection(
                                                title = slider.title ?: "",
                                                brands = slider.categories ?: slider.products ?: emptyList()
                                            )
                                        } else {
                                            CategorySlider(
                                                title = slider.title ?: "",
                                                categories = slider.categories ?: slider.products ?: emptyList()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Loading -> {
                        item(span = { GridItemSpan(3) }) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    else -> {}
                }


                when (val state = productsState) {
                    is UiState.Success -> {
                        items(state.data) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Simple pagination trigger
                            LaunchedEffect(state.data.size) {
                                if (state.data.indexOf(product) == state.data.size - 1) {
                                    viewModel.loadNextPage(categoryId)
                                }
                            }
                        }
                    }
                    is UiState.Loading -> {
                        items(6) {
                            ProductCardShimmer()
                        }
                    }
                    is UiState.Error -> {
                        item(span = { GridItemSpan(3) }) {
                            Text(text = state.message, modifier = Modifier.padding(16.dp))
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun CategoryTopBar(title: String, onBackClick: () -> Unit) {
    Surface(
        color = colorResource(id = R.color.primary),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_bold))
            )
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist")
            }
        }
    }
}

@Composable
fun CategoryBottomActions() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF333333))
                .clickable { /* TODO */ },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FILTER",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold))
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(colorResource(id = R.color.error))
                .clickable { /* TODO */ },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sort),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SORT BY",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold))
                )
            }
        }
    }
}
