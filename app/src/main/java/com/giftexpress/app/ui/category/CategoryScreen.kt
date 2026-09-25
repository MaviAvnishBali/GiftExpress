package com.giftexpress.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.ui.home.*
import com.giftexpress.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: Int,
    categoryName: String,
    viewModel: CategoryViewModel,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    addedSkus: Set<String>,
    onSubCategoryClick: (categoryId: Int, name: String) -> Unit = { _, _ -> },
    onSpecialClick: (specialFlag: Int, title: String) -> Unit = { _, _ -> }, // Top Sellers / Featured / New Arrivals shortcuts
    onCategoryArrowClick: (() -> Unit)? = null, // "Shop By Category" section arrow → Categories tab
    onBrandArrowClick: (() -> Unit)? = null,     // "Popular ..." section arrow → Brands tab
    onBrandItemClick: ((Int, String) -> Unit)? = null,

    onSearchClick: () -> Unit = {},
    cartCount: Int = 0,
    onAddToWishlist: (String) -> Unit = {}
) {
    val categoryDataState by viewModel.categoryDataState.collectAsState()
    val productsState by viewModel.productsState.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val selectedManufacturer by viewModel.selectedManufacturer.collectAsState()
    val brands by viewModel.brandsState.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor = Color.White
        ) {
            SortBottomSheetContent(
                currentSort = currentSort,
                onSortSelected = { option ->
                    viewModel.applySort(categoryId, option)
                    showSortSheet = false
                },
                onClearSort = {
                    viewModel.clearSort(categoryId)
                    showSortSheet = false
                }
            )
        }
    }

    val fallbackTitle = when (categoryId) {
        4 -> "Women's Fragrances"
        3 -> "Men's Fragrances"
        16 -> "Unisex Fragrances"
        else -> ""
    }
    val bannerTitle = (categoryDataState as? UiState.Success)?.data
        ?.firstOrNull { it.type == "banner" }?.banners?.firstOrNull()?.title
    val resolvedCategoryTitle = categoryName.ifBlank {
        bannerTitle?.takeIf { it.isNotBlank() } ?: fallbackTitle.ifBlank { "Category" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CategoryTopBar(
                    title = resolvedCategoryTitle,
                    onBackClick = onBackClick,
                    onCartClick = onCartClick,
                    onSearchClick = onSearchClick,
                    cartCount = cartCount
                )
            },
        bottomBar = {
            val currentSelectedFilters by viewModel.selectedFilters.collectAsState()
            CategoryBottomActions(
                onFilterClick = { showFilterSheet = true },
                onSortClick = { showSortSheet = true },
                sortLabel = if (currentSort != null && currentSort != CategoryViewModel.SortOption.IN_STOCK) currentSort?.label else null,
                filterLabel = if (currentSelectedFilters.isNotEmpty()) "Filtered" else null
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                        item(span = { GridItemSpan(3) }) { androidx.compose.material3.Text("DEBUG: Sliders size ${sliders.size}", modifier = Modifier.fillMaxWidth().background(Color.Red).padding(16.dp)) }
                        sliders.forEach { slider ->
                            when (slider.type) {
                                "banner" -> {
                                    val validBanners = slider.banners?.filter { !it.mobImage.isNullOrBlank() }
                                    if (!validBanners.isNullOrEmpty()) {
                                        item(span = { GridItemSpan(3) }) {
                                            HeroBanner(
                                                banners = validBanners,
                                                cornerRadius = 20.dp,
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                                // Some (sub)categories deliver their banner section as an "offer"
                                // slider rather than "banner". Render it so the images aren't dropped.
                                "offer" -> {
                                    item(span = { GridItemSpan(3) }) {
                                        slider.offers?.let { offers ->
                                            OffersSection(
                                                title = slider.title ?: "",
                                                offers = offers
                                            )
                                        }
                                    }
                                }
                                "category" -> {
                                    item(span = { GridItemSpan(3) }) {
                                        if (slider.title?.contains("Popular", ignoreCase = true) == true) {
                                            BrandSection(
                                                title = slider.title,
                                                brands = slider.categories ?: slider.products ?: emptyList(),
                                                onArrowClick = onBrandArrowClick,
                                                onBrandClick = { brand ->
                                                    val brandId = brand.id?.toIntOrNull() ?: brand.categoryId ?: 0
                                                    if (brandId > 0) {
                                                        onBrandItemClick?.invoke(brandId, brand.name ?: "Brand")
                                                    } else {
                                                        brand.sku?.let(onProductClick)
                                                    }
                                                }
                                            )
                                        } else {
                                            CategorySlider(
                                                title = slider.title ?: "",
                                                categories = slider.categories ?: slider.products ?: emptyList(),
                                                onArrowClick = onCategoryArrowClick,
                                                onCategoryClick = { product ->
                                                    val name = product.name ?: ""
                                                    // iOS routes these shortcuts by name to a category-scoped special listing
                                                    val specialFlag = when {
                                                        name.contains("Top Sellers", ignoreCase = true) -> 15   // bestSelling
                                                        name.contains("Featured", ignoreCase = true) -> 14      // featuredProducts
                                                        name.contains("New Arrival", ignoreCase = true) -> 17   // newArrival
                                                        else -> null
                                                    }
                                                    val catId = product.categoryId ?: product.id?.toIntOrNull()
                                                    when {
                                                        specialFlag != null -> onSpecialClick(specialFlag, name)
                                                        catId != null && catId > 0 -> onSubCategoryClick(catId, name)
                                                        else -> product.sku?.let(onProductClick)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    val validBanners = slider.banners?.filter { !it.mobImage.isNullOrBlank() }
                                    if (!validBanners.isNullOrEmpty()) {
                                        item(span = { GridItemSpan(3) }) {
                                            HeroBanner(
                                                banners = validBanners,
                                                cornerRadius = 20.dp,
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
                        if (state.data.isEmpty()) {
                            item(span = { GridItemSpan(3) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp, horizontal = 24.dp),
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
                            }
                        } else {
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
                                
                                // Simple pagination trigger
                                LaunchedEffect(state.data.size) {
                                    if (state.data.indexOf(product) == state.data.size - 1) {
                                        viewModel.loadNextPage(categoryId)
                                    }
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
        
    if (showFilterSheet) {
            val apiFilters by viewModel.apiFiltersState.collectAsState()
            val currentSelectedFilters by viewModel.selectedFilters.collectAsState()
            
            FilterFullScreen(
                filters = apiFilters,
                initialSelectedFilters = currentSelectedFilters,
                onApply = { newFilters ->
                    viewModel.applyFilters(categoryId, newFilters)
                    showFilterSheet = false
                },
                onBackClick = { showFilterSheet = false },
                onClearFilter = {
                    viewModel.clearFilter(categoryId)
                }
            )
        }
    }
}

@Composable
fun CategoryTopBar(
    title: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    cartCount: Int = 0
) {
    Surface(
        color = colorResource(id = R.color.primary),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            // Matches iOS: search + cart (with live count badge). No wishlist button.
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            Box {
                IconButton(onClick = onCartClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cart_black),
                        contentDescription = "Cart",
                        modifier = Modifier.size(24.dp)
                    )
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
}

@Composable
fun CategoryBottomActions(
    onFilterClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    sortLabel: String? = null,
    filterLabel: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (filterLabel != null) Color(0xFF1B5E20) else Color(0xFF333333))
                    .clickable(onClick = onFilterClick),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (filterLabel != null) "FILTERED" else "FILTER",
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
                    .background(if (sortLabel != null) Color(0xFFB71C1C) else Color(0xFFCC1414))
                    .clickable(onClick = onSortClick),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sort),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (sortLabel != null) "SORTED" else "SORT BY",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold))
                    )
                }
            }
        }
    }
}

@Composable
fun FilterFullScreen(
    filters: List<com.giftexpress.app.data.model.ProductFilter>?,
    initialSelectedFilters: Map<String, List<String>>,
    onApply: (Map<String, List<String>>) -> Unit,
    onBackClick: () -> Unit,
    onClearFilter: () -> Unit
) {
    var selectedFilters by remember { mutableStateOf(initialSelectedFilters) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val currentCategory = filters?.getOrNull(selectedCategoryIndex)
    val categoryOptions = currentCategory?.options ?: emptyList()
    val filteredOptions = if (searchQuery.isBlank()) categoryOptions
    else categoryOptions.filter { it.label?.contains(searchQuery, ignoreCase = true) == true }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
                Text(
                    text = "Filters",
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    color = Color.Black
                )
                TextButton(onClick = { 
                    selectedFilters = emptyMap()
                    onClearFilter()
                }) {
                    Text("Clear All", color = Color(0xFFE53935), fontFamily = FontFamily(Font(R.font.gilroy_medium)), fontSize = 14.sp)
                }
            }

            // Split View
            Row(modifier = Modifier.weight(1f)) {
                // Left side: Categories
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .background(Color(0xFFF2F2F7))
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filters?.size ?: 0) { index ->
                            val category = filters!![index]
                            val isSelected = index == selectedCategoryIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFFE5E5EA) else Color.Transparent)
                                    .clickable { 
                                        selectedCategoryIndex = index
                                        searchQuery = "" 
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.name ?: "",
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily(Font(R.font.gilroy_medium))
                                )
                            }
                            Divider(color = Color.LightGray.copy(0.3f))
                        }
                    }
                }

                // Right side: Values
                Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color(0xFFF2F2F7), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                            fontSize = 14.sp,
                            color = Color.Black
                        ),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("Search", fontSize = 14.sp, color = Color.Gray, fontFamily = FontFamily(Font(R.font.gilroy_regular)))
                            }
                            inner()
                        }
                    )
                    
                    if (filters == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    } else if (categoryOptions.isEmpty()) {
                         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No options", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredOptions) { option ->
                                val requestVar = currentCategory?.requestVar ?: ""
                                val isSelected = selectedFilters[requestVar]?.contains(option.value) == true
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            val currentValues = selectedFilters[requestVar]?.toMutableList() ?: mutableListOf()
                                            if (isSelected) {
                                                currentValues.remove(option.value)
                                            } else {
                                                option.value?.let { currentValues.add(it) }
                                            }
                                            val newFilters = selectedFilters.toMutableMap()
                                            if (currentValues.isEmpty()) {
                                                newFilters.remove(requestVar)
                                            } else {
                                                newFilters[requestVar] = currentValues
                                            }
                                            selectedFilters = newFilters
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val parsedLabel = option.label?.let {
                                        androidx.core.text.HtmlCompat.fromHtml(it, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                                    } ?: ""
                                    Text(
                                        text = parsedLabel,
                                        fontFamily = FontFamily(Font(if (isSelected) R.font.gilroy_bold else R.font.gilroy_regular)),
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Divider(color = Color.LightGray.copy(0.3f))
                            }
                        }
                    }
                }
            }

            // Bottom Apply Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onApply(selectedFilters) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Apply Filters",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold))
                    )
                }
            }
        }
    }
}

@Composable
fun SortBottomSheetContent(
    currentSort: CategoryViewModel.SortOption?,
    onSortSelected: (CategoryViewModel.SortOption) -> Unit,
    onClearSort: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SORT BY",
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                fontSize = 16.sp,
                color = Color.Gray
            )
            if (currentSort != null && currentSort != CategoryViewModel.SortOption.IN_STOCK) {
                Text(
                    text = "Reset",
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    fontSize = 14.sp,
                    color = Color(0xFFE53935),
                    modifier = Modifier.clickable(onClick = onClearSort)
                )
            }
        }
        Divider(color = Color.LightGray.copy(0.5f))

        CategoryViewModel.SortOption.entries.forEach { option ->
            val isSelected = currentSort == option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSortSelected(option) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.label,
                    fontFamily = FontFamily(Font(if (isSelected) R.font.gilroy_bold else R.font.gilroy_regular)),
                    fontSize = 16.sp,
                    color = if (isSelected) Color(0xFF1976D2) else Color.Black
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Divider(color = Color.LightGray.copy(0.3f), modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
