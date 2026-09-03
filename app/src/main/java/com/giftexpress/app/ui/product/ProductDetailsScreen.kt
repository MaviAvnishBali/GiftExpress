package com.giftexpress.app.ui.product

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.res.ResourcesCompat
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.ProductDetail
import com.giftexpress.app.data.model.ProductDetailsResponse
import com.giftexpress.app.data.model.ProductReview
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.ui.home.ProductCard
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState
import kotlin.math.roundToInt

@Composable
fun ProductDetailsScreen(
    sku: String,
    fallbackImageUrl: String? = null,
    viewModel: ProductDetailsViewModel,
    addedSkus: Set<String>,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMainAddToCart: (String, Int) -> Unit,
    onProductCardAddToCart: (String) -> Unit,
    onProductCardGoToCart: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToWishlist: ((String) -> Unit)? = null,
    cartCount: Int = 0
) {
    val uiState by viewModel.productState.collectAsState()
    var quantity by remember { mutableStateOf(1) }
    val cartState by viewModel.cartState.collectAsState()
    var isAddedToCart by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductDetail?>(null) }
    val context = LocalContext.current

    LaunchedEffect(sku) {
        viewModel.getProductDetails(sku)
        isAddedToCart = false
    }

    LaunchedEffect(cartState) {
        when (cartState) {
            is UiState.Success -> {
                isAddedToCart = true
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
            }
            is UiState.Error -> {
                Toast.makeText(context, (cartState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            ProductTopBar(
                onBackClick = onBackClick,
                onCartClick = onCartClick,
                onSearchClick = onSearchClick,
                cartCount = cartCount,
                onWishlistClick = {
                    val currentSku = (uiState as? UiState.Success)
                        ?.data?.productDetails?.firstOrNull()?.sku
                    if (currentSku != null) onAddToWishlist?.invoke(currentSku)
                }
            )
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                val response = (uiState as UiState.Success<ProductDetailsResponse>).data
                if (selectedProduct == null) {
                    selectedProduct = response.productDetails?.firstOrNull()
                }
                val product = selectedProduct
                val unitPrice = product?.discountPrice ?: product?.price ?: 0.0
                val originalUnitPrice = if (product?.discountPrice != null) product.price else null
                BottomBar(
                    sellingPrice = unitPrice * quantity,
                    originalPrice = originalUnitPrice?.times(quantity),
                    isLoading = cartState is UiState.Loading,
                    isAddedToCart = isAddedToCart,
                    onAddToCart = {
                        if (isAddedToCart) {
                            onCartClick()
                        } else {
                            product?.sku?.let { onMainAddToCart(it, quantity) }
                        }
                    }
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    val productResponse = state.data
                    ProductContent(
                        productResponse = productResponse,
                        quantity = quantity,
                        onQuantityChange = { quantity = it },
                        addedSkus = addedSkus,
                        onProductCardAddToCart = onProductCardAddToCart,
                        onProductCardGoToCart = onProductCardGoToCart,
                        onProductClick = onProductClick,
                        selectedProduct = selectedProduct,
                        onVariantSelected = { selectedProduct = it },
                        viewModel = viewModel,
                        onAddToWishlist = onAddToWishlist,
                        fallbackImageUrl = fallbackImageUrl
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProductTopBar(onBackClick: () -> Unit, onCartClick: () -> Unit, onSearchClick: () -> Unit, onWishlistClick: () -> Unit = {}, cartCount: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Search Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSearchClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search...",
                    fontFamily = Gilroy,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = onWishlistClick) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Add to Wishlist",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Box {
            IconButton(onClick = onCartClick) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart",
                    tint = Color.Black,
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

/**
 * True if the URL points at a real image file. Rejects blanks and the broken Magento
 * placeholder path that has an empty filename (".../placeholder/.jpg").
 */
private fun String.isUsableImageUrl(): Boolean {
    if (isBlank()) return false
    val fileName = substringAfterLast('/').substringBeforeLast('.', missingDelimiterValue = "")
    return fileName.isNotBlank()
}

/** Format a monetary amount to 2 decimals, avoiding float artefacts like 251.5499999999999. */
private fun Double.asPrice(): String = String.format(java.util.Locale.US, "%.2f", this)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductContent(
    productResponse: ProductDetailsResponse,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    addedSkus: Set<String>,
    onProductCardAddToCart: (String) -> Unit,
    onProductCardGoToCart: () -> Unit,
    onProductClick: (String) -> Unit,
    selectedProduct: ProductDetail?,
    onVariantSelected: (ProductDetail) -> Unit,
    viewModel: ProductDetailsViewModel? = null,
    onAddToWishlist: ((String) -> Unit)? = null,
    fallbackImageUrl: String? = null
) {
    val product = selectedProduct ?: productResponse.productDetails?.firstOrNull() ?: return
    val scrollState = rememberScrollState()
    var showTesterPopup by remember { mutableStateOf(false) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    if (zoomImageUrl != null) {
        ZoomableImageDialog(imageUrl = zoomImageUrl!!, onDismiss = { zoomImageUrl = null })
    }

    if (showTesterPopup) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTesterPopup = false },
            title = {
                Text(
                    text = "Tester Information",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = product.attributes?.tester ?: "No information available",
                    fontFamily = Gilroy,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showTesterPopup = false }) {
                    Text(
                        "Close",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Image Carousel.
        // Some variants (e.g. testers) return a broken Magento placeholder with an empty
        // filename (".../placeholder/.jpg" → 404). Drop those, and if the selected variant
        // has no usable image fall back to the first variant's images (matches iOS:
        // selectedProduct?.images ?? productList.first?.images).
        val variantImages = product.images.orEmpty().filter { it.isUsableImageUrl() }
        val images = variantImages.ifEmpty {
            productResponse.productDetails?.firstOrNull()?.images.orEmpty().filter { it.isUsableImageUrl() }
        }.ifEmpty {
            listOfNotNull(fallbackImageUrl?.takeIf { it.isUsableImageUrl() })
        }
        if (images.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { images.size })
            // Reset to first image whenever the selected variant changes
            LaunchedEffect(product.sku) {
                if (pagerState.currentPage != 0) pagerState.scrollToPage(0)
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .clickable { zoomImageUrl = images[page] },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = images[page],
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Pager Indicator
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(images.size) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) Color(0xFF4CAF50) else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Product Name
            Text(
                text = product.name ?: "",
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Brand
            Text(
                text = product.manufacturer ?: "",
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Price Section — unit prices drive the per-quantity total shown below
            val sellingPrice = product.discountPrice ?: product.price ?: 0.0
            val originalPrice = if (product.discountPrice != null) product.price else null
            // Displayed amounts update dynamically with the selected quantity
            val displaySellingPrice = sellingPrice * quantity
            val displayOriginalPrice = originalPrice?.times(quantity)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$${displaySellingPrice.asPrice()}",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )

                    if (displayOriginalPrice != null && displayOriginalPrice > displaySellingPrice) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${displayOriginalPrice.asPrice()}",
                            fontFamily = Gilroy,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Quantity Selector
                Row(
                    modifier = Modifier
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("-", fontSize = 20.sp, color = Color.Gray)
                    }
                    Text(
                        text = quantity.toString(),
                        fontFamily = Gilroy,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", fontSize = 20.sp, color = Color.Gray)
                    }
                }
            }

            // Savings Row
            if (originalPrice != null && originalPrice > sellingPrice) {
                Row {
                    val savings = (originalPrice - sellingPrice) * quantity
                    val savingsPercent = (((originalPrice - sellingPrice) / originalPrice) * 100).roundToInt()

                    Text(
                        text = "SAVINGS - $${savings.asPrice()} ($savingsPercent%)",
                        fontFamily = Gilroy,
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50) // Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Free Shipping & Stock
            Text(
                text = "Free U.S. Shipping on orders over $59.95.",
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "In stock",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Table
            ProductInfoRow("SKU", product.sku ?: "")
            ProductInfoRow("Type", product.attributes?.type ?: "")
            ProductInfoRow("Selected Size", product.size ?: "")
            ProductInfoRow("Gender", product.attributes?.gender ?: "")
            ProductInfoRow("UPC", product.attributes?.upc ?: "")
            ProductInfoRow(
                label = "Tester",
                value = product.attributes?.tester ?: "No",
                isHelp = true,
                onHelpClick = { showTesterPopup = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Select Sizes
            Text(
                text = "Select Sizes",
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Size Grid/List
            productResponse.productDetails?.let { variants ->
                val firstValidImage = variants.mapNotNull { it.images?.firstOrNull() }
                    .firstOrNull { !it.contains("placeholder") }
                val finalFallback = firstValidImage ?: fallbackImageUrl

                SizeSelectionList(
                    variants = variants,
                    selectedVariant = selectedProduct,
                    fallbackImage = finalFallback,
                    onVariantSelected = onVariantSelected
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val moreProducts = productResponse.moreProductList.orEmpty()
            if (moreProducts.isNotEmpty()) {
                Text(
                    text = "MORE FROM ${product.manufacturer?.uppercase()}",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(moreProducts) { moreProduct ->
                        ProductCard(
                            product = SliderProduct(
                                id = moreProduct.productId,
                                name = moreProduct.name,
                                price = moreProduct.price,
                                image = moreProduct.image ?: "",
                                sku = moreProduct.sku,
                                attributes = moreProduct.attributes
                            ),
                            onProductClick = { clickedSku ->
                                onProductClick(clickedSku)
                            },
                            onAddToCart = { moreProduct.sku?.let(onProductCardAddToCart) },
                            onGoToCart = onProductCardGoToCart,
                            isAdded = moreProduct.sku?.let { addedSkus.contains(it) } == true,
                            onAddToWishlist = { moreProduct.sku?.let { onAddToWishlist?.invoke(it) } }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tabs (Details, Shipping, Reviews)
            ProductTabsSection(product = product, sku = product.sku ?: "", viewModel = viewModel)

            Spacer(modifier = Modifier.height(24.dp))


            Spacer(modifier = Modifier.height(80.dp))
        }
        }
    }

@Composable
fun ProductInfoRow(
    label: String,
    value: String,
    isHelp: Boolean = false,
    onHelpClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            if (isHelp) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Help",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onHelpClick?.invoke() }
                )
            }
        }

        if (!isHelp) {
            Text(
                text = value,
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SizeSelectionList(
    variants: List<ProductDetail>,
    selectedVariant: ProductDetail?,
    fallbackImage: String?,
    onVariantSelected: (ProductDetail) -> Unit
) {
    val chunkedVariants = variants.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        chunkedVariants.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { variant ->
                    Box(modifier = Modifier.weight(1f)) {
                        val isSoldOut = variant.size?.contains("59") == true
                        SizeItem(
                            variant = variant,
                            isSelected = variant.sku == selectedVariant?.sku,
                            isSoldOut = isSoldOut,
                            fallbackImage = fallbackImage,
                            onClick = { if (!isSoldOut) onVariantSelected(variant) }
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SizeItem(
    variant: ProductDetail,
    isSelected: Boolean,
    isSoldOut: Boolean,
    fallbackImage: String?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF333333)
        isSoldOut -> Color(0xFFF5F5F5)
        else -> Color.White
    }

    val textColor = if (isSelected) Color.White else Color.Black
    val subTextColor = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.Gray

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected || isSoldOut) 0.dp else 2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSoldOut) { onClick() }
            .border(
                width = 1.dp,
                color = when {
                    isSelected -> Color.Transparent
                    isSoldOut -> Color.Transparent
                    else -> Color.LightGray.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rawImage = variant.images?.firstOrNull()
            val finalImage = if (rawImage == null || rawImage.contains("placeholder")) fallbackImage else rawImage
            
            AsyncImage(
                model = finalImage,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .graphicsLayer {
                        if (isSoldOut) alpha = 0.5f
                    },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = variant.size ?: "",
                    fontFamily = Gilroy,
                    fontSize = 13.sp,
                    color = subTextColor
                )
                val sellingPrice = variant.discountPrice ?: variant.price ?: 0.0
                Text(
                    text = "$${sellingPrice.asPrice()}",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textColor
                )

                if (isSoldOut) {
                    Text(
                        text = "Sold Out",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Red
                    )
                    Text(
                        text = "Notify Me",
                        fontFamily = Gilroy,
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                } else {
                    Text(
                        text = "In stock",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF00C853)
                    )
                    Text(
                        text = "Ready To Ship",
                        fontFamily = Gilroy,
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun ProductTabsSection(product: ProductDetail, sku: String = "", viewModel: ProductDetailsViewModel? = null) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Details", "Shipping Information", "Review")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 2 && sku.isNotBlank() && viewModel != null) {
            viewModel.loadReviews(sku)
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFFE53935) else Color(0xFFF0F0F0))
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = Gilroy,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else Color(0xFF333333)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> DetailsTab(product)
            1 -> HtmlText(
                html = product.shippingInformation?.details ?: "No shipping info",
                modifier = Modifier.fillMaxWidth()
            )
            2 -> ReviewsSection(sku = sku, viewModel = viewModel)
        }
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    // Rendered in a WebView: TextView + Html.fromHtml collapses <ul>/<li>/<h*> and
    // ignores CSS, which left the Shipping Information tab unformatted. Magento template
    // directives like {{store url='...'}} are stripped so they don't leak into the output.
    val cleaned = html.replace(Regex("\\{\\{.*?\\}\\}"), "")
    val styledHtml = """
        <html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <style>
        body { font-family: sans-serif; font-size: 14px; color: #333333; margin: 0; padding: 0; line-height: 1.5; }
        h1 { font-size: 18px; margin: 12px 0 8px; }
        h2 { font-size: 16px; margin: 12px 0 6px; }
        p { margin: 0 0 10px; }
        ul { padding-left: 20px; margin: 0 0 10px; }
        li { margin-bottom: 4px; }
        a { color: #1976D2; }
        img { max-width: 100%; height: auto; }
        </style></head><body>$cleaned</body></html>
    """.trimIndent()
    AndroidView(
        modifier = modifier.fillMaxWidth().heightIn(min = 80.dp),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        }
    )
}

@Composable
fun DetailsTab(product: ProductDetail) {
    Column {
        HtmlText(
            html = product.details?.description ?: "",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "More Information",
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        val details = product.details
        ProductInfoRow("UPC", details?.upc ?: "")
        ProductInfoRow("Shop By In stock", "0.000000")
        ProductInfoRow("Manufacturer", details?.manufacturer ?: "")
        ProductInfoRow("Gender", details?.gender ?: "")
        ProductInfoRow("Special", "New Arrivals")
        ProductInfoRow("Perfume Type", details?.perfumeType ?: "")
        ProductInfoRow("Sizes", details?.size ?: "")
    }
}

@Composable
fun ReviewsSection(sku: String = "", viewModel: ProductDetailsViewModel? = null) {
    val reviewsStateFlow = viewModel?.reviewsState
    val submitStateFlow = viewModel?.submitReviewState
    val reviewsState by (reviewsStateFlow?.collectAsState() ?: remember { mutableStateOf<UiState<List<ProductReview>>>(UiState.Idle) })
    val submitState by (submitStateFlow?.collectAsState() ?: remember { mutableStateOf<UiState<Boolean>>(UiState.Idle) })
    var showWriteForm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
            showWriteForm = false
            viewModel?.resetSubmitReviewState()
        }
        if (submitState is UiState.Error) {
            Toast.makeText(context, (submitState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel?.resetSubmitReviewState()
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Customer Reviews",
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Button(
                onClick = { showWriteForm = !showWriteForm },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (showWriteForm) "CANCEL" else "WRITE A REVIEW",
                    fontFamily = Gilroy,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        if (showWriteForm && viewModel != null) {
            Spacer(Modifier.height(16.dp))
            WriteReviewForm(
                sku = sku,
                isSubmitting = submitState is UiState.Loading,
                onSubmit = { nickname, title, detail, rating ->
                    viewModel.submitReview(sku, nickname, title, detail, rating)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = reviewsState) {
            is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(32.dp))
            is UiState.Error -> Text(state.message, color = Color.Gray, fontFamily = Gilroy, fontSize = 13.sp)
            is UiState.Success -> {
                val reviews = state.data
                if (reviews.isEmpty()) {
                    Text("No reviews yet. Be the first!", fontFamily = Gilroy, color = Color.Gray, fontSize = 13.sp)
                } else {
                    reviews.forEach { review ->
                        ReviewCard(review = review)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun WriteReviewForm(
    sku: String,
    isSubmitting: Boolean,
    onSubmit: (nickname: String, title: String, detail: String, rating: Int) -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Write a Review", fontFamily = Gilroy, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Star rating selector
        Row {
            repeat(5) { index ->
                val filled = index < rating
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (filled) Color(0xFFFFC107) else Color.LightGray,
                    modifier = Modifier.size(32.dp).clickable { rating = index + 1 }
                )
            }
        }

        ReviewTextField("Nickname *", nickname) { nickname = it }
        ReviewTextField("Review Title *", title) { title = it }
        ReviewTextField("Review *", detail) { detail = it }

        Button(
            onClick = {
                if (nickname.isNotBlank() && title.isNotBlank() && detail.isNotBlank()) {
                    onSubmit(nickname, title, detail, rating)
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
            shape = RoundedCornerShape(4.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("SUBMIT REVIEW", fontFamily = Gilroy, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ReviewTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontFamily = Gilroy, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(4.dp))
                .padding(10.dp)
        )
    }
}

@Composable
private fun ReviewCard(review: ProductReview) {
    val avgRating = review.averageRating()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                repeat(5) { i ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < avgRating.toInt()) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(review.nickname, fontFamily = Gilroy, fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(6.dp))
        Text(review.title, fontFamily = Gilroy, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        Text(review.detail, fontFamily = Gilroy, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
    }
}

@Composable
fun ZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += panChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                    .transformable(state = transformState),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun BottomBar(
    sellingPrice: Double,
    originalPrice: Double? = null,
    isLoading: Boolean = false,
    isAddedToCart: Boolean = false,
    onAddToCart: () -> Unit = {}
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        ) {
            Button(
                onClick = onAddToCart,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B2B2B),
                    disabledContainerColor = Color(0xFF2B2B2B).copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isAddedToCart) {
                            Text(
                                text = "GO TO CART",
                                fontFamily = Gilroy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "ADD TO CART",
                                fontFamily = Gilroy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color.White.copy(alpha = 0.5f))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "$${sellingPrice.asPrice()}",
                                fontFamily = Gilroy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )

                            if (originalPrice != null && originalPrice > sellingPrice) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$${originalPrice.asPrice()}",
                                    fontFamily = Gilroy,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
