package com.giftexpress.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
    searchPlaceholder: String = "Search for 'Perfume'",
    onSearchBarClick: (() -> Unit)? = null,
    cartCount: Int = 0
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
                Box {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = "Cart",
                            tint = Color(0xFFFBDB98),
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .then(
                    if (onSearchBarClick != null) Modifier.clickable { onSearchBarClick() }
                    else Modifier
                ),
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
                if (onSearchBarClick != null) {
                    // Non-interactive placeholder — clicking the card navigates to Search
                    Text(
                        text = searchPlaceholder,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
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
    contentPadding: PaddingValues = PaddingValues(12.dp),
    onBannerClick: ((SliderBanner) -> Unit)? = null
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll logic
    androidx.compose.runtime.LaunchedEffect(pagerState.settledPage) {
        if (banners.size > 1) {
            kotlinx.coroutines.delay(3000)
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
            val banner = banners[page]
            SubcomposeAsyncImage(
                model = banner.mobImage,
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .then(
                        // Match iOS: every banner is tappable; the handler decides what (if
                        // anything) to do based on type (product/category/brand). Use
                        // detectTapGestures rather than Modifier.clickable — a plain clickable
                        // inside a HorizontalPager competes with the pager's horizontal drag
                        // detector and frequently loses, so taps never fire (this was the B-01
                        // "banner tap does not redirect" bug). pointerInput + detectTapGestures
                        // coexists with the drag gesture and reliably delivers the tap.
                        if (onBannerClick != null)
                            Modifier.pointerInput(banner) {
                                detectTapGestures { onBannerClick(banner) }
                            }
                        else Modifier
                    ),
                contentScale = ContentScale.Fit,
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
    onCategoryClick: (SliderProduct) -> Unit,
    onArrowClick: (() -> Unit)? = null   // iOS: tabBarController.selectedIndex = 1
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
                modifier = Modifier
                    .size(16.dp)
                    .then(if (onArrowClick != null) Modifier.clickable { onArrowClick() } else Modifier),
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
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .width(90.dp)
            .height(108.dp)
            .clickable { onCategoryClick(category) }
    ) {
        // Image: 70x70, scaleAspectFit, centered horizontally, at top
        SubcomposeAsyncImage(
            model = category.image,
            contentDescription = category.name,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            }
        )

        // Gap = 79 - 70 = 9dp (matches iOS label.frame.y - image bottom)
        Spacer(modifier = Modifier.height(9.dp))

        // Label: bold 10sp, centered, max 2 lines, 5dp horizontal padding
        Text(
            text = category.name ?: "",
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black,
            lineHeight = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
        )
    }
}

@Composable
fun ProductSection(
    title: String,
    products: List<SliderProduct>,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    addedSkus: Set<String>,
    onGoToCart: () -> Unit,
    onSeeAllClick: ((specialFlag: Int, title: String) -> Unit)? = null,
    onSliderSeeAllClick: ((categoryId: Int?, title: String) -> Unit)? = null,
    onAddToWishlist: (String) -> Unit = {}
) {
    val specialFlag = when {
        title.contains("best sell", ignoreCase = true) -> 15
        title.contains("new arriv", ignoreCase = true) -> 17
        title.contains("featured", ignoreCase = true) -> 14
        else -> null
    }
    // Non-special sliders (e.g. Men's / Women's Fragrances, Ahuja Products) are routed
    // by the Fragment via title; category sliders also carry a category_id on their products.
    val sectionCategoryId = products.firstOrNull { it.categoryId != null }?.categoryId
    val arrowAction: (() -> Unit)? = when {
        specialFlag != null && onSeeAllClick != null -> { { onSeeAllClick(specialFlag, title) } }
        onSliderSeeAllClick != null -> { { onSliderSeeAllClick(sectionCategoryId, title) } }
        else -> null
    }
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
                modifier = Modifier
                    .size(16.dp)
                    .then(
                        if (arrowAction != null)
                            Modifier.clickable { arrowAction() }
                        else Modifier
                    ),
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
                    isAdded = isAdded,
                    onAddToWishlist = { product.sku?.let(onAddToWishlist) }
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
    onAddToWishlist: (() -> Unit)? = null,
    isWishlistedInitial: Boolean = false,
    modifier: Modifier = Modifier.width(140.dp)
) {
    Column(
        modifier = modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { product.sku?.let { onProductClick(it) } }

    ) {

        if (onAddToWishlist != null) {
            var isWishlistedLocal by remember { mutableStateOf(isWishlistedInitial) }
            Icon(
                painter = painterResource(id = if (isWishlistedLocal) R.drawable.ic_heart else R.drawable.ic_heart_outlined),
                contentDescription = "Wishlist",
                tint = if (isWishlistedLocal) Color(0xFFFF6B6B) else Color.Black,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 0.dp, end = 8.dp)
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null // Removes the default circular ripple
                    ) {
                        isWishlistedLocal = !isWishlistedLocal
                        onAddToWishlist.invoke()
                    }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
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
            text = product.subtitle,
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
                text = "$${String.format(java.util.Locale.US, "%.2f", product.price ?: 0.0)}",
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
fun BrandSection(
    title: String,
    brands: List<SliderProduct>,
    onArrowClick: (() -> Unit)? = null,      // iOS: tabBarController.selectedIndex = 2
    onBrandClick: ((SliderProduct) -> Unit)? = null  // iOS: navigateToProductLiting(brand)
) {
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
                modifier = Modifier
                    .size(16.dp)
                    .then(if (onArrowClick != null) Modifier.clickable { onArrowClick() } else Modifier),
                tint = colorResource(id = R.color.primary)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(brands) { brand ->
                BrandItem(brand, onClick = onBrandClick?.let { { it(brand) } })
            }
        }
    }
}

@Composable
fun BrandItem(brand: SliderProduct, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .width(91.dp)
            .height(80.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
                val fallbackName = brand.url?.substringAfterLast("/")?.removeSuffix(".html")
                    ?.replace("-", " ")
                    ?.split(" ")
                    ?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
                Text(
                    text = brand.name ?: fallbackName ?: "",
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
    modifier: Modifier = Modifier,
    onOfferClick: ((SliderOffer) -> Unit)? = null   // iOS: offer.type based navigation
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
                val offer = offers[page]
                SubcomposeAsyncImage(
                    model = offer.image,
                    contentDescription = "Offer",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            // Same as HeroBanner: use detectTapGestures so the tap isn't
                            // swallowed by the pager's horizontal drag detector.
                            if (onOfferClick != null && offer.urlApi.isNotBlank())
                                Modifier.pointerInput(offer) {
                                    detectTapGestures { onOfferClick(offer) }
                                }
                            else Modifier
                        ),
                    contentScale = ContentScale.Fit,
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
    ProductCard(
        product = sampleProduct, 
        onProductClick = {},
        onAddToWishlist = {}
    )
}