package com.giftexpress.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.giftexpress.app.data.model.SliderProduct

val Gilroy = FontFamily(
    Font(R.font.gilroy_regular, FontWeight.Normal),
    Font(R.font.gilroy_medium, FontWeight.Medium),
    Font(R.font.gilroy_bold, FontWeight.Bold)
)

@Composable
fun HomeHeader(onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.primary))
            .padding(bottom = 16.dp)
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
                modifier = Modifier.height(40.dp)
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
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_outline),
                        contentDescription = "Profile",
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
                    fontFamily = Gilroy
                )
            }
        }
    }
}

@Composable
fun HeroBanner(banners: List<SliderBanner>) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    Box(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            AsyncImage(
                model = banners[page].mobImage,
                contentDescription = "Banner",
                modifier = Modifier.fillMaxSize(),
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
                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
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
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
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
            shape = CircleShape,
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
            fontFamily = Gilroy,
            fontWeight = FontWeight.Medium,
            color = colorResource(id = R.color.primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
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
            .width(160.dp)
            .padding(8.dp)
    ) {
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
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(id = R.drawable.ic_gift)
            )
            
            Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "Favorite",
                tint = colorResource(id = R.color.favorite_red),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(16.dp)
            )
            
            // Rating Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = product.name ?: "",
            fontSize = 12.sp,
            fontFamily = Gilroy,
            fontWeight = FontWeight.Medium,
            color = colorResource(id = R.color.primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = "Eau De Parfum",
            fontSize = 10.sp,
            fontFamily = Gilroy,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "As Low As ",
                fontSize = 10.sp,
                fontFamily = Gilroy,
                color = colorResource(id = R.color.primary)
            )
            Text(
                text = product.price ?: "",
                fontSize = 14.sp,
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.primary)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            contentPadding = PaddingValues(vertical = 8.dp),
            border = null,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Add to cart",
                    color = colorResource(id = R.color.primary),
                    fontSize = 11.sp,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_shopping_bag),
                    contentDescription = null,
                    tint = colorResource(id = R.color.primary),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun BrandSection(brands: List<Brand>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "POPULAR BRANDS",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colorResource(id = R.color.primary)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(brands) { brand ->
                BrandItem(brand)
            }
        }
    }
}

@Composable
fun BrandItem(brand: Brand) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_gift),
                contentDescription = brand.name,
                modifier = Modifier.padding(12.dp),
                contentScale = ContentScale.Fit
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
