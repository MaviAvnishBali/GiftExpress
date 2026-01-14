package com.giftexpress.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.MenuItem
import com.giftexpress.app.data.model.SliderBanner
import com.giftexpress.app.data.model.SliderOffer
import com.giftexpress.app.data.model.SliderProduct
import com.giftexpress.app.ui.components.shimmerEffect


@Composable
fun HomeHeader(
    banners: List<SliderBanner>? = null,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.primary))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    .height(55.dp)
                    .width(110.dp)
            )

            Row {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cart),
                        contentDescription = "Cart",
                        tint = Color.White,
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search for 'Perfume'",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_regular))
                )
            }
        }

        Spacer(modifier = Modifier
            .height(20.dp)
            .fillMaxWidth())

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

    Box(modifier = modifier
        .fillMaxWidth()
        .padding(contentPadding)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            AsyncImage(
                model = banners[page].mobImage,
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_gift)
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
fun CategorySection(categories: List<MenuItem>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "SHOP BY CATEGORY",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryItem(category)
            }
        }
    }
}

@Composable
fun CategoryItem(category: MenuItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Card(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(70.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_gift),
                contentDescription = category.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Inside
            )
        }
        Text(
            text = category.title,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = colorResource(id = R.color.primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategorySlider(title: String, categories: List<SliderProduct>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title.uppercase(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategorySliderItem(category)
            }
        }
    }
}

@Composable
fun CategorySliderItem(category: SliderProduct) {
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
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = category.image,
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_gift)
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
fun ProductSection(title: String, products: List<SliderProduct>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "More",
                modifier = Modifier.size(16.dp),
                tint = colorResource(id = R.color.primary)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                ProductCard(product)
            }
        }
    }
}

@Composable
fun ProductCard(product: SliderProduct) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .padding(5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)

    ) {

        // IMAGE SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(id = R.color.product_image_bg))
        ) {

            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                modifier = Modifier
                    .size(92.dp)
                    .padding(top = 10.dp)
                    .padding(16.dp),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.logo)
            )

            // Heart Icon
            Box(
                modifier = Modifier
                    .padding(top = 9.dp, end = 8.dp)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = "Favorite",
                    tint = colorResource(id = R.color.favorite_red),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Rating Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        tint = colorResource(id = R.color.accent_gold),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "4.9",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(712 reviews)",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily(Font(R.font.gilroy_regular))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PRODUCT NAME
        Text(
            text = product.name ?: "Product",
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = colorResource(id = R.color.primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SUBTITLE
        Text(
            text = "Eau De Parfum",
            fontSize = 11.sp,
            color = Color.Gray,
            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
        )

        Spacer(modifier = Modifier.height(6.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        // ADD TO CART BUTTON
        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color.LightGray),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Text(
                text = "Add to cart",
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
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
            .background(colorResource(id = R.color.section_bg))
            .padding(vertical = 36.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                fontSize = 14.sp,
                color = colorResource(id = R.color.primary)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "More",
                modifier = Modifier.size(16.dp),
                tint = colorResource(id = R.color.primary)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
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
            AsyncImage(
                model = brand.image,
                contentDescription = brand.name,
                modifier = Modifier.padding(12.dp),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.ic_gift)
            )
        }
    }
}

@Composable
fun PromoBanner(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Promo Banner",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(150.dp),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = R.drawable.ic_gift)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OffersSection(
    offers: List<SliderOffer>,
    modifier: Modifier = Modifier
) {
    if (offers.isEmpty()) return

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            text = "OFFERS",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary)
        )

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
                AsyncImage(
                    model = offers[page].image,
                    contentDescription = "Offer",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillBounds,
                    placeholder = painterResource(id = R.drawable.ic_gift)
                )
            }
        }
    }
}