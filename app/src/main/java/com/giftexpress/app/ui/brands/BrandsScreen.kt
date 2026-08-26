package com.giftexpress.app.ui.brands

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.category.CategoryBottomActions
import com.giftexpress.app.ui.category.FilterFullScreen
import com.giftexpress.app.ui.category.SortBottomSheetContent
import com.giftexpress.app.ui.home.HomeHeader
import com.giftexpress.app.ui.home.ProductCard
import com.giftexpress.app.ui.home.ProductCardShimmer
import com.giftexpress.app.ui.products.SpecialProductsViewModel
import com.giftexpress.app.utils.UiState

@Composable
fun BrandsScreen(
    viewModel: BrandViewModel,
    specialViewModel: SpecialProductsViewModel,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    addedSkus: Set<String>,
    cartCount: Int,
    onAddToWishlist: (String) -> Unit = {}
) {
    // Master-detail within the Brands tab: the brand list, and — when a brand is tapped — its
    // products shown inline on the same screen, reusing the unified SpecialProductsScreen
    // (brand mode) rather than navigating away.
    var selected by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val brand = selected

    if (brand == null) {
        AllBrandsScreen(
            viewModel = viewModel,
            onMenuClick = onMenuClick,
            onCartClick = onCartClick,
            onBrandClick = { id, name -> selected = id to name },
            cartCount = cartCount
        )
    } else {
        // System back returns to the brand list instead of leaving the tab.
        BackHandler { selected = null }
        BrandProductsInline(
            brandId = brand.first,
            brandName = brand.second,
            viewModel = specialViewModel,
            addedSkus = addedSkus,
            cartCount = cartCount,
            onBackClick = { selected = null },
            onMenuClick = onMenuClick,
            onCartClick = onCartClick,
            onSearchClick = onSearchClick,
            onProductClick = onProductClick,
            onAddToCart = onAddToCart,
            onGoToCart = onGoToCart,
            onAddToWishlist = onAddToWishlist
        )
    }
}

/**
 * Brand products shown inline in the Brands tab — top bar with back button and brand title
 * over a 3-column product grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandProductsInline(
    brandId: Int,
    brandName: String,
    viewModel: SpecialProductsViewModel,
    addedSkus: Set<String>,
    cartCount: Int,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    onAddToWishlist: (String) -> Unit = {}
) {
    LaunchedEffect(brandId) { viewModel.loadProducts(specialFlag = 0, brandId = brandId, reset = true) }

    val productsState by viewModel.productsState.collectAsState()
    val gridState = rememberLazyGridState()
    val products = (productsState as? UiState.Success)?.data ?: emptyList()

    val shouldLoadMore by remember(products, gridState) {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            products.isNotEmpty() && last >= products.size - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadProducts(specialFlag = 0, brandId = brandId)
    }

    val currentSort by viewModel.currentSort.collectAsState()
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
                onClearFilter = { viewModel.clearFilter() }
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(color = androidx.compose.ui.res.colorResource(id = R.color.primary), contentColor = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = brandName,
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        color = Color.White
                    )
                    IconButton(onClick = onSearchClick) {
                        Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            val currentSelectedFilters by viewModel.selectedFilters.collectAsState()
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
            is UiState.Loading -> LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) { items(9) { ProductCardShimmer() } }

            is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = Color.Gray)
            }

            is UiState.Success -> {
                if (products.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products available", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(products) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onProductClick,
                                onAddToCart = { product.sku?.let(onAddToCart) },
                                onGoToCart = onGoToCart,
                                isAdded = product.sku?.let { addedSkus.contains(it) } == true,
                                onAddToWishlist = { product.sku?.let(onAddToWishlist) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusable(),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_regular))
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = "Search Brands",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.gilroy_regular))
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
