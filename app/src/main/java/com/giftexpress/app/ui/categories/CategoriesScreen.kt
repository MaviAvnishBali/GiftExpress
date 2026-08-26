package com.giftexpress.app.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.MenuResponse
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.utils.UiState

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onCategoryClick: (categoryId: Int, title: String) -> Unit = { _, _ -> },
    onSpecialClick: (specialFlag: Int, title: String, bannerUrl: String?) -> Unit = { _, _, _ -> }
) {
    val categoriesState by viewModel.categoriesState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when (val state = categoriesState) {
            is UiState.Loading -> CategoriesShimmer()

            is UiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.message, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.fetchCategories() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                    ) { Text("Retry", color = Color.White) }
                }
            }

            is UiState.Success -> {
                val categories = state.data

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Where\nEvery\nFragrance\nTells a\nStory.",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            lineHeight = 36.sp,
                            color = Color(0xFFE5B0E1), // Light pink/purple matching iOS
                            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                        )
                    }

                    // Category cells — matching iOS height 260dp each
                    items(categories) { menuResponse ->
                        val menu = menuResponse.menu ?: return@items
                        val bannerUrl = menu.submenu?.sections?.flatMap { it.banners ?: emptyList() }?.firstOrNull()?.mobImage

                        CategoryItemCell(
                            imageUrl = menu.displayImage,
                            title = menu.title,
                            modifier = Modifier.height(260.dp),
                            onClick = {
                                val id = menu.categoryId ?: 0
                                val title = menu.title ?: ""
                                if (title.equals("Best Sellers", ignoreCase = true)) {
                                    onSpecialClick(15, title, bannerUrl)
                                } else {
                                    onCategoryClick(id, title)
                                }
                            }
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

/**
 * Category image cell — iOS shows full-bleed image with scaleAspectFill, cornerRadius 18
 */
@Composable
private fun CategoryItemCell(
    imageUrl: String?,
    title: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        if (imageUrl.isNullOrBlank()) {
            // Fallback when no image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title ?: "",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
        }
    }
}

@Composable
private fun CategoriesShimmer() {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) { index ->
            Box(
                Modifier
                    .height(260.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .shimmerEffect()
            )
        }
    }
}
