package com.giftexpress.app.ui.brands

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.BrandResponse
import com.giftexpress.app.utils.UiState
import com.giftexpress.app.ui.home.HomeHeader
import kotlinx.coroutines.launch

@Composable
fun AllBrandsScreen(
    viewModel: BrandViewModel,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onBrandClick: (Int, String) -> Unit
) {
    val brandsState by viewModel.brandsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isScrolled = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0

    // Group brands by first letter
    val groupedBrands = remember(brandsState, searchQuery) {
        when (val state = brandsState) {
            is UiState.Success -> {
                val filtered = if (searchQuery.isBlank()) {
                    state.data
                } else {
                    state.data.filter { 
                        it.name?.contains(searchQuery, ignoreCase = true) == true 
                    }
                }
                filtered.groupBy { 
                    it.name?.firstOrNull()?.uppercaseChar() ?: '#'
                }.toSortedMap()
            }
            else -> emptyMap()
        }
    }

    // Find which letter is currently visible
    val visibleLetter = remember(listState, groupedBrands) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty() || groupedBrands.isEmpty()) {
                return@derivedStateOf groupedBrands.keys.firstOrNull() ?: 'A'
            }
            
            val firstVisibleItem = layoutInfo.visibleItemsInfo.first()
            val itemIndex = firstVisibleItem.index
            
            // Skip the "ALL BRANDS" header (index 0)
            if (itemIndex == 0) {
                return@derivedStateOf groupedBrands.keys.firstOrNull() ?: 'A'
            }
            
            // Find which letter section this item belongs to
            // Start from index 1 (after "ALL BRANDS" header)
            var currentIndex = 1
            for (letter in groupedBrands.keys) {
                val sectionSize = (groupedBrands[letter]?.size ?: 0) + 1 // +1 for letter header
                if (itemIndex < currentIndex + sectionSize) {
                    return@derivedStateOf letter
                }
                currentIndex += sectionSize
            }
            groupedBrands.keys.lastOrNull() ?: 'A'
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeHeader(
            banners = null,
            onMenuClick = onMenuClick,
            onCartClick = onCartClick,
            isScrolled = isScrolled,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            searchPlaceholder = "Search Brands"
        )

        Box(modifier = Modifier.weight(1f)) {
            // Brands List
            when (val state = brandsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message ?: "Error loading brands",
                            color = Color.Red
                        )
                    }
                }
                is UiState.Success -> {
                    if (groupedBrands.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No brands available" else "No brands found",
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                // ALL BRANDS Header
                                item {
                                    Text(
                                        text = "ALL BRANDS",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                
                                groupedBrands.forEach { (letter, brands) ->
                                    // Section Header
                                    item {
                                        Text(
                                            text = letter.toString(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF5F5F5))
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            fontSize = 18.sp,
                                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // Brand Items
                                    items(brands) { brand ->
                                        BrandListItem(
                                            brand = brand,
                                            onClick = {
                                                val brandId = brand.id ?: 0
                                                val brandName = brand.name ?: "Brand"
                                                onBrandClick(brandId, brandName)
                                            }
                                        )
                                    }
                                }
                            }

                            // Alphabetical Index
                            AlphabeticalIndex(
                                letters = groupedBrands.keys.toList(),
                                selectedLetter = visibleLetter.value,
                                onLetterClick = { letter ->
                                    // Scroll to letter section
                                    coroutineScope.launch {
                                        // Start from index 1 (after "ALL BRANDS" header)
                                        var targetIndex = 1
                                        for (l in groupedBrands.keys) {
                                            if (l == letter) {
                                                listState.animateScrollToItem(targetIndex)
                                                return@launch
                                            }
                                            // Add 1 for letter header + brands count
                                            targetIndex += (groupedBrands[l]?.size ?: 0) + 1
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 8.dp)
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun BrandListItem(
    brand: BrandResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (!brand.image.isNullOrEmpty()) {
                AsyncImage(
                    model = brand.image,
                    contentDescription = brand.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder with initials
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = brand.name?.take(2)?.uppercase() ?: "BR",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        color = Color.Black
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Brand Name
        Text(
            text = brand.name ?: "Unknown Brand",
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = Color.Black
        )
    }
    
    Divider(
        modifier = Modifier.padding(start = 80.dp),
        color = Color(0xFFE0E0E0),
        thickness = 0.5.dp
    )
}

@Composable
fun AlphabeticalIndex(
    letters: List<Char>,
    selectedLetter: Char,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        letters.forEach { letter ->
            Text(
                text = letter.toString(),
                modifier = Modifier
                    .clickable { onLetterClick(letter) }
                    .padding(vertical = 2.dp),
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                color = if (letter == selectedLetter) {
                    colorResource(id = R.color.error)
                } else {
                    Color.Black
                }
            )
        }
    }
}
