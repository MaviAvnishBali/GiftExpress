package com.giftexpress.app.ui.brands

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R

@Composable
fun BrandsScreen(
    viewModel: BrandViewModel,
    onBackClick: () -> Unit,
    onBrandClick: (Int, String) -> Unit,
    onProductClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCart: (String) -> Unit,
    onGoToCart: () -> Unit,
    addedSkus: Set<String>
) {
    val selectedBrand by viewModel.selectedBrand.collectAsState()

    if (selectedBrand == null) {
        AllBrandsScreen(
            viewModel = viewModel,
            onMenuClick = onMenuClick,
            onCartClick = onCartClick,
            onBrandClick = { id, name ->
                viewModel.selectBrand(id, name)
            }
        )
    } else {
        selectedBrand?.let { (id, name) ->
            BrandProductsScreen(
                brandId = id,
                brandName = name,
                viewModel = viewModel,
                onBackClick = {
                    viewModel.clearSelectedBrand()
                },
                onProductClick = onProductClick,
                onCartClick = onCartClick,
                onMenuClick = onMenuClick,
                onAddToCart = onAddToCart,
                onGoToCart = onGoToCart,
                addedSkus = addedSkus
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            text = "Search Brands",
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
