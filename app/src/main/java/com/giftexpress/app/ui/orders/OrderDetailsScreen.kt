package com.giftexpress.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.OrderApiResponse
import com.giftexpress.app.data.model.OrderApiItem
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.utils.UiState

@Composable
fun OrderDetailsScreen(
    viewModel: OrderDetailsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.orderState.collectAsState()

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
                        text = "Order Details",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFF8F8F8)
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is UiState.Loading -> Box(Modifier.fillMaxSize().shimmerEffect())
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color.Gray)
                    }
                }
                is UiState.Success -> OrderDetailsContent(order = s.data)
                else -> {}
            }
        }
    }
}

@Composable
private fun OrderDetailsContent(order: OrderApiResponse) {
    val shippingAddress = order.getShippingAddress() ?: order.billingAddress

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Order Header
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Order #${order.incrementId}",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        val statusColor = when (order.status?.lowercase()) {
                            "complete" -> Color(0xFF2E7D32)
                            "canceled", "closed" -> Color(0xFFC62828)
                            else -> Color(0xFFF57C00)
                        }
                        Text(
                            text = order.status?.replaceFirstChar { it.uppercase() } ?: "",
                            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                            fontSize = 14.sp,
                            color = statusColor
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", order.grandTotal ?: 0.0)}",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // Items
        order.items?.let { items ->
            item {
                Text(
                    text = "ITEMS",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(items) { item ->
                OrderItemRow(item = item)
            }
        }

        // Shipping Address
        shippingAddress?.let { addr ->
            item {
                SectionCard {
                    LabelValue("SHIPPING ADDRESS", "")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = addr.fullName(),
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = addr.fullAddress(),
                        fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                    addr.telephone?.let {
                        Text(
                            text = it,
                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Billing Address
        order.billingAddress?.let { addr ->
            item {
                SectionCard {
                    LabelValue("BILLING ADDRESS", "")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = addr.fullName(),
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = addr.fullAddress(),
                        fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                    addr.telephone?.let {
                        Text(
                            text = it,
                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Shipping Method
        order.shippingDescription?.let { method ->
            item {
                SectionCard {
                    LabelValue("SHIPPING METHOD", method)
                }
            }
        }

        // Price Breakdown
        item {
            SectionCard {
                LabelValue("Subtotal", "$${String.format("%.2f", order.subtotal ?: 0.0)}")
                if ((order.shippingAmount ?: 0.0) > 0) {
                    LabelValue("Shipping", "$${String.format("%.2f", order.shippingAmount)}")
                }
                if ((order.taxAmount ?: 0.0) > 0) {
                    LabelValue("Tax", "$${String.format("%.2f", order.taxAmount)}")
                }
                if ((order.discountAmount ?: 0.0) != 0.0) {
                    LabelValue(
                        "Discount${order.couponCode?.let { " ($it)" } ?: ""}",
                        "-$${String.format("%.2f", Math.abs(order.discountAmount ?: 0.0))}",
                        valueColor = Color(0xFF2E7D32)
                    )
                }
                if ((order.extensionAttributes?.amextrafeeFeeAmount ?: 0.0) > 0.0) {
                    LabelValue("Shipping Protection", "$${String.format("%.2f", order.extensionAttributes?.amextrafeeFeeAmount)}")
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(0.5f))
                LabelValue(
                    label = "Total",
                    value = "$${String.format("%.2f", order.grandTotal ?: 0.0)}",
                    labelWeight = FontWeight.Bold,
                    fontSize = 16
                )
            }
        }

        // Payment Method
        item {
            SectionCard {
                LabelValue("PAYMENT METHOD", order.getPaymentTitle())
                // Show card last 4 digits if available (Stripe/credit card payments)
                val cardNumber = order.payment?.ccLast4
                    ?: order.paymentAdditionalInfo?.firstOrNull { it.key == "card_number" || it.key == "cc_last_4" }?.value
                    ?: order.extensionAttributes?.paymentAdditionalInfo?.firstOrNull { it.key == "card_number" || it.key == "cc_last_4" }?.value
                
                val cardDisplay = cardNumber?.let { if (it.length <= 4) "**** $it" else it }
                if (!cardDisplay.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    LabelValue("Card", cardDisplay)
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderApiItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(0.5.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = item.extensionAttributes?.productImage,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_box),
            placeholder = painterResource(R.drawable.ic_box)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name ?: "",
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            item.extensionAttributes?.availableSizes?.let { size ->
                Text(
                    text = "Size: $size",
                    fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Qty: ${item.qtyOrdered ?: 1}",
                    fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "$${String.format("%.2f", item.price ?: 0.0)}",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray.copy(0.5f), RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun LabelValue(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    labelWeight: FontWeight = FontWeight.Normal,
    fontSize: Int = 14
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            fontWeight = labelWeight,
            fontSize = fontSize.sp,
            color = if (labelWeight == FontWeight.Bold) Color.Black else Color.Gray,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Text(
            text = value,
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontWeight = labelWeight,
            fontSize = fontSize.sp,
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}
