package com.giftexpress.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.SliderBanner
import com.giftexpress.app.data.model.SliderOffer
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.ui.components.shimmerEffect


@Composable
fun HomeHeader(
    banners: List<SliderBanner>? = null,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    isScrolled: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    searchPlaceholder: String = "Search for 'Perfume'"
) {
    val headerVerticalPadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 12.dp,
        label = "headerPadding"
    )
    val bottomSpacerHeight by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isScrolled) 8.dp else 20.dp,
        label = "bottomSpacer"
    )
    val logoHeight by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isScrolled) 40.dp else 55.dp,
        label = "logoHeight"
    )

    var query by remember { mutableStateOf(searchQuery) }
    
    // Update local query when external query changes
    androidx.compose.runtime.LaunchedEffect(searchQuery) {
        query = searchQuery
    }
    
    // Notify parent when query changes
    val onQueryChange: (String) -> Unit = { newQuery ->
        query = newQuery
        onSearchQueryChange?.invoke(newQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.primary))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = headerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.logo_gold_white),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(logoHeight)
                    .width(110.dp)
            )

            Row {
                IconButton(onClick = onCartClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cart),
                        contentDescription = "Cart",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                text = searchPlaceholder,
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

        Spacer(
            modifier = Modifier
                .height(bottomSpacerHeight)
                .fillMaxWidth()
        )

        if (!banners.isNullOrEmpty()) {
            HeroBanner(
                banners = banners,
                cornerRadius = 0.dp,
                contentPadding = PaddingValues(0.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroBanner(
    banners: List<SliderBanner>,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp)
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll logic
    androidx.compose.runtime.LaunchedEffect(pagerState.settledPage) {
        if (banners.size > 1) {
            kotlinx.coroutines.delay(3000) // 3 seconds delay
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.settledPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            SubcomposeAsyncImage(
                model = banners[page].mobImage,
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                }
            )
        }

        // Pager Indicators
        Row(
            Modifier
                .height(20.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(banners.size) { iteration ->
                val width by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp,
                    label = "indicator"
                )
                val color =
                    if (pagerState.currentPage == iteration) Color.Green else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(8.dp)
                        .width(width)
                )
            }
        }
    }
}

@Composable
fun CategorySlider(
    title: String,
    categories: List<SliderProduct>,
    onCategoryClick: (SliderProduct) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.akrobat_semi_bold)),
                fontSize = 20.sp,
                color = colorResource(id = R.color.primary)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "More",
                modifier = Modifier.size(16.dp),
                tint = colorResource(id = R.color.primary)
            )
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategorySliderItem(category, onCategoryClick)
            }
        }
    }
}

@Composable
fun CategorySliderItem(
    category: SliderProduct,
    onCategoryClick: (SliderProduct) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(101.dp)
            .height(118.dp)
    ) {
        Card(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable { onCategoryClick(category) },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SubcomposeAsyncImage(
                    model = category.image,
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                    }
                )

                Text(
                    text = category.name ?: "",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 11.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

    }
}

@Composable
fun ProductSection(
    title: String,
    products: List<SliderProduct>,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    addedSkus: Set<String>,
    onGoToCart: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.akrobat_semi_bold)),
                fontSize = 20.sp,
                color = colorResource(id = R.color.primary)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "More",
                modifier = Modifier.size(16.dp),
                tint = colorResource(id = R.color.primary)
            )
        }
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                val isAdded = product.sku?.let { addedSkus.contains(it) } == true
                ProductCard(
                    product = product,
                    onProductClick = onProductClick,
                    onAddToCart = { product.sku?.let(onAddToCart) },
                    onGoToCart = onGoToCart,
                    isAdded = isAdded
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: SliderProduct,
    onProductClick: (String) -> Unit,
    onAddToCart: () -> Unit = {},
    onGoToCart: () -> Unit = {},
    isAdded: Boolean = false,
    modifier: Modifier = Modifier.width(140.dp)
) {
    Column(
        modifier = modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { product.sku?.let { onProductClick(it) } }

    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(id = R.color.product_image_bg))
        ) {
            SubcomposeAsyncImage(
                model = product.image,
                contentDescription = product.name,
                loading = {
                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }


        Spacer(modifier = Modifier.height(12.dp))

        // PRODUCT NAME
        Text(
            text = product.name ?: "Product",
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = colorResource(id = R.color.primary),
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SUBTITLE
        Text(
            text = "Eau De Perfume",
            fontSize = 11.sp,
            color = Color.Gray,
            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
        )

        Spacer(modifier = Modifier.height(10.dp))

        // PRICE
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "As Low As ",
                fontSize = 11.sp,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.gilroy_regular)),
            )
            Text(
                text = "$${product.price ?: 0.0}",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                color = colorResource(id = R.color.primary)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // ADD TO CART BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp, bottom = 4.dp) // room for shadow
        ) {

            // SHADOW
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        shadowElevation = 6f
                        shape = RoundedCornerShape(4.dp)
                        clip = false
                        translationX = 2f
                        translationY = 2f
                        ambientShadowColor = Color.Black
                        spotShadowColor = Color.Black
                    }
            )

            // BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        color = colorResource(id = R.color.primary),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { if (isAdded) onGoToCart() else onAddToCart() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isAdded) "Go to cart" else "Add to cart",
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_semi_bold)),
                        color = colorResource(id = R.color.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shopping_bag),
                        contentDescription = null,
                        tint = colorResource(id = R.color.primary),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }



    }
}


@Composable
fun ProductCardShimmer() {
    Column(
        modifier = Modifier
            .width(170.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect()
        )
    }
}


@Composable
fun BrandSection(title: String, brands: List<SliderProduct>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .background(colorResource(id = R.color.section_bg))
            .padding(bottom = 30.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.akrobat_semi_bold)),
                fontSize = 20.sp,
                color = colorResource(id = R.color.primary)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "More",
                modifier = Modifier.size(16.dp),
                tint = colorResource(id = R.color.primary)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(brands) { brand ->
                BrandItem(brand)
            }
        }
    }
}

@Composable
fun BrandItem(brand: SliderProduct) {
    Card(
        modifier = Modifier
            .width(91.dp)
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!brand.image.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = brand.image,
                    contentDescription = brand.name,
                    modifier = Modifier.padding(12.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                    }
                )
            } else {
                Text(
                    text = brand.name ?: "",
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun PromoBanner(imageUrl: String) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = "Promo Banner",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(150.dp),
        contentScale = ContentScale.Crop,
        loading = {
            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OffersSection(
    title: String,
    offers: List<SliderOffer>,
    modifier: Modifier = Modifier
) {
    if (offers.isEmpty()) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.akrobat_semi_bold)),
                fontSize = 20.sp,
                color = colorResource(id = R.color.primary)
            )
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(20.dp))

        val pagerState = rememberPagerState(pageCount = { offers.size })
        
        // Auto-scroll logic
        androidx.compose.runtime.LaunchedEffect(pagerState.settledPage) {
            if (offers.size > 1) {
                kotlinx.coroutines.delay(3000) // 3 seconds delay
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.settledPage + 1) % offers.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 8.dp
            ) { page ->
                SubcomposeAsyncImage(
                    model = offers[page].image,
                    contentDescription = "Offer",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillBounds,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    val sampleProduct = SliderProduct(
        name = "Chanel No. 5",
        price = 125.00,
        image = "", // Placeholder will be used
        sku = "12345",
        id = "1"
    )
    ProductCard(product = sampleProduct, onProductClick = {})
}