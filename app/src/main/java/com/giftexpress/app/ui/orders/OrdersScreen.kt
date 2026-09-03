package com.giftexpress.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.OrderApiResponse
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onBackClick: () -> Unit,
    onOrderClick: (Int) -> Unit
) {
    val state by viewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF333333), contentColor = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "My Orders",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFF8F8F8)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is UiState.Loading -> OrdersShimmer()
                is UiState.Error -> ErrorState(message = s.message, onRetry = { viewModel.loadOrders() })
                is UiState.Success -> {
                    val orders = s.data
                    if (orders.isEmpty()) {
                        EmptyOrdersState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders) { order ->
                                OrderCard(order = order, onClick = {
                                    order.entityId?.let { onOrderClick(it) }
                                })
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderApiResponse, onClick: () -> Unit) {
    val firstItem = order.items?.firstOrNull()
    val imageUrl = firstItem?.extensionAttributes?.productImage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_box),
                placeholder = painterResource(R.drawable.ic_box)
            )

            Column(modifier = Modifier.weight(1f)) {
                val statusColor = when (order.status?.lowercase()) {
                    "complete", "delivered" -> Color(0xFF2E7D32)
                    "canceled", "closed" -> Color(0xFFC62828)
                    else -> Color(0xFFF57C00)
                }

                Text(
                    text = order.status?.replaceFirstChar { it.uppercase() } ?: "Processing",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 14.sp,
                    color = statusColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = firstItem?.name ?: "Order #${order.incrementId}",
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Order #${order.incrementId}",
                    fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                val dateStr = order.createdAt?.let { formatDate(it) } ?: ""
                if (dateStr.isNotBlank()) {
                    Text(
                        text = dateStr,
                        fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.2f", order.grandTotal ?: 0.0)}",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp).align(Alignment.CenterVertically),
                tint = Color.Gray
            )
        }
    }
}

private fun formatDate(raw: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(raw) ?: return raw
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) { raw }
}

@Composable
private fun OrdersShimmer() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).shimmerEffect())
        }
    }
}

@Composable
private fun EmptyOrdersState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_box),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.LightGray
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Please continue shopping or log in to your account to view your previous orders.",
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                text = message,
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}
