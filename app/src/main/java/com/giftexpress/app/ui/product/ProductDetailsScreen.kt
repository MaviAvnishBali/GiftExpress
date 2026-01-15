package com.giftexpress.app.ui.product

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.data.model.ProductDetail
import com.giftexpress.app.data.model.ProductDetailsResponse
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.ui.home.ProductCard
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState
import kotlin.math.roundToInt

@Composable
fun ProductDetailsScreen(
    sku: String,
    viewModel: ProductDetailsViewModel,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    LaunchedEffect(sku) {
        viewModel.getProductDetails(sku)
    }

    val uiState by viewModel.productState.collectAsState()

    Scaffold(
        topBar = {
            ProductTopBar(onBackClick = onBackClick, onCartClick = onCartClick)
        },
        bottomBar = {
            if (uiState is UiState.Success) {
                val product = (uiState as UiState.Success).data.productDetails?.firstOrNull()
                val sellingPrice = product?.discountPrice ?: product?.price ?: 0.0
                val originalPrice = if (product?.discountPrice != null) product.price else null
                BottomBar(sellingPrice = sellingPrice, originalPrice = originalPrice)
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Text(
                        text = (uiState as UiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    val productResponse = (uiState as UiState.Success).data
                    ProductContent(productResponse = productResponse)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProductTopBar(onBackClick: () -> Unit, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Row {
            IconButton(onClick = { /* TODO: Search */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onCartClick) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "Cart",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { /* TODO: Wishlist */ }) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = Color.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductContent(productResponse: ProductDetailsResponse) {
    var selectedProduct by remember(productResponse) { 
        mutableStateOf(productResponse.productDetails?.firstOrNull()) 
    }
    val product = selectedProduct ?: return
    val scrollState = rememberScrollState()
    var quantity by remember { mutableStateOf(1) }
    var showTesterPopup by remember { mutableStateOf(false) }

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
                    Text("Close", fontFamily = Gilroy, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
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
        // Image Carousel
        val images = product.images ?: emptyList()
        if (images.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { images.size })
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
                            .background(Color.White),
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
                        val color = if (pagerState.currentPage == iteration) Color(0xFF4CAF50) else Color.LightGray
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
            
            // Brand
            Text(
                text = product.manufacturer ?: "",
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Price Section
            val sellingPrice = product.discountPrice ?: product.price ?: 0.0
            val originalPrice = if (product.discountPrice != null) product.price else null

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$$sellingPrice",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                    
                    if (originalPrice != null && originalPrice > sellingPrice) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$$originalPrice",
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
                        onClick = { if (quantity > 1) quantity-- },
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
                        onClick = { quantity++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", fontSize = 20.sp, color = Color.Gray)
                    }
                }
            }

            // Savings Row
            if (originalPrice != null && originalPrice > sellingPrice) {
                Row {
                    val savings = originalPrice - sellingPrice
                    val savingsPercent = ((savings / originalPrice) * 100).roundToInt()

                    Text(
                        text = "SAVINGS - $$savings ($savingsPercent%)",
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
                SizeSelectionList(
                    variants = variants,
                    selectedVariant = product,
                    onVariantSelected = { selectedProduct = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // More From Brand
            Text(
                text = "MORE FROM ${product.manufacturer?.uppercase()}",
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            productResponse.moreProductList?.let { moreProducts ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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

                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tabs (Details, Shipping, Reviews)
            ProductTabsSection(product)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reviews Section
            ReviewsSection()
            
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
                        // Mocking sold out state for demonstration if SKU contains "59" (matching the image's 59ml example)
                        val isSoldOut = variant.size?.contains("59") == true 
                        SizeItem(
                            variant = variant,
                            isSelected = variant.sku == selectedVariant?.sku,
                            isSoldOut = isSoldOut,
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
            AsyncImage(
                model = variant.images?.firstOrNull(),
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
                    text = "$$sellingPrice",
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
                        color = Color(0xFF00C853) // Brighter green for contrast
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
fun ProductTabsSection(product: ProductDetail) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Details", "Shipping Information", "Review")
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Spacing between buttons
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
            1 -> Text(
                text = product.shippingInformation?.details ?: "No shipping info",
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
            2 -> ReviewsSection()
        }
    }
}

@Composable
fun DetailsTab(product: ProductDetail) {
    Column {
        Text(
            text = product.details?.description ?: "",
            fontFamily = Gilroy,
            fontSize = 14.sp,
            color = Color.Black,
            lineHeight = 20.sp
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
fun ReviewsSection() {
    Column {
        Text(
            text = "Customer Review",
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = "4.8",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "out of stars",
                    fontFamily = Gilroy,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row {
                repeat(5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFCDDC39),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "WRITE A REVIEW",
                    fontFamily = Gilroy,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun BottomBar(sellingPrice: Double, originalPrice: Double? = null) {
    Surface(
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { /* TODO: Add to cart */ },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ADD TO CART",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Text(
                        text = "$$sellingPrice",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    if (originalPrice != null && originalPrice > sellingPrice) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$$originalPrice",
                            fontFamily = Gilroy,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                 }
            }
        }
    }
}
