package com.giftexpress.app.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.data.model.CartItem
import com.giftexpress.app.ui.theme.Gilroy

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBackClick: () -> Unit,
    onCheckout: (() -> Unit)? = null,
    onProductClick: (String) -> Unit = {},
    onContinueShopping: (() -> Unit)? = null
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val shipping by viewModel.shipping.collectAsState()
    val tax by viewModel.tax.collectAsState()
    val total by viewModel.total.collectAsState()

    Scaffold(
        topBar = {
            CartTopBar(
                cartCount = cartItems.sumOf { it.quantity },
                onBackClick = onBackClick,
                onClearClick = { viewModel.clearCart() },
                showClearAll = cartItems.isNotEmpty()
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty() && onCheckout != null) {
                Surface(shadowElevation = 8.dp, color = Color.White) {
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "PROCEED TO CHECKOUT",
                            fontFamily = Gilroy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (error != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error ?: "An error occurred",
                                color = Color.Red,
                                fontFamily = Gilroy,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onQuantityChange = { newQty -> viewModel.updateQuantity(item.id, newQty) },
                        onRemove = { viewModel.removeItem(item.id) },
                        onProductClick = { if (item.sku.isNotBlank()) onProductClick(item.sku) }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                }

                if (cartItems.isNotEmpty()) {
                    item {
                        CartSummary(
                            subtotal = subtotal,
                            shipping = shipping,
                            tax = tax,
                            total = total
                        )
                    }
                } else if (!isLoading && error == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Your cart is empty",
                                    fontFamily = Gilroy,
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                if (onContinueShopping != null) {
                                    Button(
                                        onClick = onContinueShopping,
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA444A)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Continue Shopping",
                                            fontFamily = Gilroy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun CartTopBar(cartCount: Int, showClearAll: Boolean = false, onClearClick: () -> Unit = {}, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Shopping Cart",
            fontFamily = Gilroy,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        if (showClearAll) {
            Text(
                text = "Clear All",
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Red,
                modifier = Modifier
                    .clickable { onClearClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onProductClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.image,
            contentDescription = item.name,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onProductClick() },
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.name,
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%.2f", item.price)}",
                    fontFamily = Gilroy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "-",
                        fontSize = 24.sp,
                        color = Color.Red,
                        modifier = Modifier
                            .clickable { if (item.quantity > 1) onQuantityChange(item.quantity - 1) }
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = item.quantity.toString(),
                        fontFamily = Gilroy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        color = Color.Red,
                        modifier = Modifier
                            .clickable { onQuantityChange(item.quantity + 1) }
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sku: ${item.sku}",
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Size: ${item.size}",
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CartSummary(
    subtotal: Double,
    shipping: Double,
    tax: Double,
    total: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD4F2EB)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SummaryRow("Subtotal", "$${String.format("%.2f", subtotal)}", isBold = false)
            
            Spacer(modifier = Modifier.height(24.dp))

            SummaryRow("Order Total", "$${String.format("%.2f", total)}", isBold = true)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Gilroy,
            fontSize = if (isBold) 18.sp else 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontFamily = Gilroy,
            fontSize = if (isBold) 18.sp else 16.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = Color.Black
        )
    }
}
