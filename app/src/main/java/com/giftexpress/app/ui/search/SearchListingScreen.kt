package com.giftexpress.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.data.model.SearchListingProduct
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun SearchListingScreen(
    searchString: String,
    viewModel: SearchListingViewModel,
    onBackClick: () -> Unit,
    onProductClick: (String, String?) -> Unit,
    addedSkus: Set<String> = emptySet(),
    onAddToCart: (String) -> Unit = {},
    onGoToCart: () -> Unit = {},
    onAddToWishlist: (String) -> Unit = {}
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val apiFilters by viewModel.apiFiltersState.collectAsState()
    val selectedFilters by viewModel.selectedFilters.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val gridState = rememberLazyGridState()
    
    var showFilter by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }

    LaunchedEffect(searchString) {
        viewModel.searchProducts(searchString)
    }

    // Pagination: detect near-bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleIndex >= totalItems - 4 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Column {
                    Text(
                        text = "Search Results",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "\"$searchString\"",
                        fontFamily = Gilroy,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (products.isEmpty() && isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            } else if (products.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (error != null) "Error: $error" else "No products found for \"$searchString\"",
                        fontFamily = Gilroy,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(products) { product ->
                            val sku = product.firstSku
                            val isAdded = sku?.let { addedSkus.contains(it) } == true
                            com.giftexpress.app.ui.home.ProductCard(
                                product = product.toSliderProduct(),
                                onProductClick = {
                                    if (!sku.isNullOrBlank()) onProductClick(sku, product.mainImage)
                                },
                                onAddToCart = { sku?.let(onAddToCart) },
                                onGoToCart = onGoToCart,
                                isAdded = isAdded,
                                onAddToWishlist = { sku?.let(onAddToWishlist) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }

                    com.giftexpress.app.ui.category.CategoryBottomActions(
                        onFilterClick = { showFilter = true },
                        onSortClick = { showSort = true },
                        sortLabel = currentSort?.label,
                        filterLabel = if (selectedFilters.isNotEmpty()) "FILTERED" else null
                    )
                }
            }
        }
    }

    if (showFilter) {
        com.giftexpress.app.ui.category.FilterFullScreen(
            filters = apiFilters,
            initialSelectedFilters = selectedFilters,
            onApply = { filters ->
                showFilter = false
                viewModel.applyFilters(filters)
            },
            onBackClick = { showFilter = false },
            onClearFilter = {
                showFilter = false
                viewModel.clearFilter()
            }
        )
    }

    if (showSort) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showSort = false },
            containerColor = Color.White
        ) {
            com.giftexpress.app.ui.category.SortBottomSheetContent(
                currentSort = currentSort,
                onSortSelected = { option ->
                    showSort = false
                    viewModel.applySort(option)
                },
                onClearSort = {
                    showSort = false
                    viewModel.applySort(null)
                }
            )
        }
    }
}

fun SearchListingProduct.toSliderProduct(): com.giftexpress.app.data.model.SliderProduct {
    val perfumeTypeAttr = attributes?.firstOrNull { it.id == "perfume_type" }
    val pType = perfumeTypeAttr?.values?.firstOrNull()?.value?.firstOrNull()
    return com.giftexpress.app.data.model.SliderProduct(
        name = this.name,
        price = this.displayPrice,
        image = this.mainImage,
        sku = this.firstSku,
        perfumeType = pType
    )
}

