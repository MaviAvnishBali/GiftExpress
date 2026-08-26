package com.giftexpress.app.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.category.CategoryBottomActions
import com.giftexpress.app.ui.category.FilterFullScreen
import com.giftexpress.app.ui.category.SortBottomSheetContent
import com.giftexpress.app.ui.home.ProductCard
import com.giftexpress.app.ui.home.ProductCardShimmer
import com.giftexpress.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialProductsScreen(
    specialFlag: Int,
    title: String,
    viewModel: SpecialProductsViewModel,
    addedSkus: Set<String>,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    categoryId: Int = 0,
    brandId: Int = 0,
    onSearchClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    cartCount: Int = 0,
    onAddToWishlist: (String) -> Unit = {},
    bannerUrl: String? = null
) {
    LaunchedEffect(specialFlag, categoryId, brandId) {
        viewModel.loadProducts(specialFlag, categoryId, brandId, reset = true)
    }

    val productsState by viewModel.productsState.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val selectedManufacturer by viewModel.selectedManufacturer.collectAsState()
    val brands by viewModel.brandsState.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showSortSheet) {
        ModalBottomSheet(onDismissRequest = { showSortSheet = false }, containerColor = Color.White) {
            SortBottomSheetContent(
                currentSort = currentSort,
                onSortSelected = { option ->
                    viewModel.applySort(option)
                    showSortSheet = false
                },
                onClearSort = {
                    viewModel.clearSort()
                    showSortSheet = false
                }
            )
        }
    }

    if (showFilterSheet) {
        val apiFilters by viewModel.apiFiltersState.collectAsState()
        val currentSelectedFilters by viewModel.selectedFilters.collectAsState()

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showFilterSheet = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            FilterFullScreen(
                filters = apiFilters,
                initialSelectedFilters = currentSelectedFilters,
                onApply = { newFilters ->
                    viewModel.applyFilters(newFilters)
                    showFilterSheet = false
                },
                onBackClick = { showFilterSheet = false },
                onClearFilter = {
                    viewModel.clearFilter()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(color = colorResource(id = R.color.primary), contentColor = Color.White) {
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
                    // Matches iOS: search + cart (with live count badge) on the listing screen.
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                        }
                        if (cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (cartCount > 99) "99+" else cartCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            val currentSelectedFilters by viewModel.selectedFilters.collectAsState()
            // Sort + Filter are shown in every listing condition (special flag / brand /
            // category), matching iOS ProductListingViewController.
            CategoryBottomActions(
                onFilterClick = { showFilterSheet = true },
                onSortClick = { showSortSheet = true },
                sortLabel = currentSort?.label,
                filterLabel = if (currentSelectedFilters.isNotEmpty()) "Filtered" else null
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = productsState) {
                is UiState.Loading -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            if (!bannerUrl.isNullOrBlank()) {
                                item(span = { GridItemSpan(2) }) {
                                    coil.compose.AsyncImage(
                                        model = bannerUrl,
                                        contentDescription = "Banner",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .heightIn(max = 200.dp)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                }
                            }
                            items(6) { ProductCardShimmer() }
                        }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.Gray)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadProducts(specialFlag, categoryId, brandId, reset = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "No Products",
                                modifier = Modifier.size(80.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Products Found",
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We couldn't find any products matching your criteria.",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            if (!bannerUrl.isNullOrBlank()) {
                                item(span = { GridItemSpan(2) }) {
                                    coil.compose.AsyncImage(
                                        model = bannerUrl,
                                        contentDescription = "Banner",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .heightIn(max = 200.dp)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                }
                            }
                            items(state.data) { product ->
                                val isAdded = product.sku?.let { addedSkus.contains(it) } == true
                                ProductCard(
                                    product = product,
                                    onProductClick = onProductClick,
                                    onAddToCart = { product.sku?.let(onAddToCart) },
                                    onGoToCart = onGoToCart,
                                    isAdded = isAdded,
                                    onAddToWishlist = { product.sku?.let(onAddToWishlist) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                LaunchedEffect(state.data.size) {
                                    if (state.data.indexOf(product) == state.data.size - 3) {
                                        viewModel.loadProducts(specialFlag, categoryId, brandId)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
