package com.giftexpress.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.giftexpress.app.data.model.SearchProductItem
import com.giftexpress.app.ui.theme.Gilroy

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    onSearch: (String) -> Unit,
    onProductClick: (String, String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val trendingTopics by viewModel.trendingTopics.collectAsState()
    val searchCategories by viewModel.searchCategories.collectAsState()
    val searchProducts by viewModel.searchProducts.collectAsState()
    val isLoadingSearch by viewModel.isLoadingSearch.collectAsState()
    val isLoadingTrending by viewModel.isLoadingTrending.collectAsState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(searchQuery) {
        viewModel.fetchAutocomplete(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top search bar row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Search for 'Perfume'",
                        fontFamily = Gilroy,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) onSearch(searchQuery)
                })
            )
        }

        if (searchQuery.isBlank()) {
            // Show trending topics
            if (isLoadingTrending) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Red)
                }
            } else if (trendingTopics.isNotEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Trending Topics",
                        fontFamily = Gilroy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trendingTopics.forEach { topic ->
                            TrendingChip(
                                topic = topic,
                                onClick = {
                                    searchQuery = topic
                                    onSearch(topic)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Show autocomplete results
            if (isLoadingSearch) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Red
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Categories section
                if (searchCategories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Categories",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(searchCategories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSearch(cat.value ?: "") }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cat.value ?: "",
                                fontFamily = Gilroy,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }

                // Products section
                if (searchProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Products",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(searchProducts) { product ->
                        SearchProductRow(
                            product = product,
                            onClick = {
                                val sku = product.sku?.firstOrNull()
                                if (!sku.isNullOrBlank()) onProductClick(sku, product.mainImage)
                            }
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }

                // No results
                if (!isLoadingSearch && searchCategories.isEmpty() && searchProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results for \"$searchQuery\"",
                                fontFamily = Gilroy,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingChip(topic: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF0F0F0))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = topic,
            fontFamily = Gilroy,
            fontSize = 13.sp,
            color = Color.Black
        )
    }
}

@Composable
fun SearchProductRow(product: SearchProductItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = product.mainImage,
            contentDescription = product.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF5F5F5)),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name ?: "",
                fontFamily = Gilroy,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!product.brand.isNullOrBlank()) {
                Text(
                    text = product.brand,
                    fontFamily = Gilroy,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            val price = product.sellingPrice ?: product.finalPrice ?: product.price
            if (price != null) {
                Text(
                    text = "$${String.format("%.2f", price)}",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
        }
    }
}
