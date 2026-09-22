package com.giftexpress.app.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.giftexpress.app.data.model.OrderApiItem
import com.giftexpress.app.ui.components.shimmerEffect
import com.giftexpress.app.utils.UiState

@Composable
fun OrderDetailsScreen(
    viewModel: OrderDetailsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.orderState.collectAsState()
    val context = LocalContext.current
    val order = (state as? UiState.Success)?.data
    var showTrackingDialog by remember { mutableStateOf(false) }

    fun openTrackingUrl(url: String?) {
        if (url.isNullOrBlank()) {
            Toast.makeText(context, "Tracking link is unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open tracking link", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF333333), contentColor = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    val trackingList = order?.trackingInformation.orEmpty()
                    Button(
                        onClick = {
                            if (trackingList.size == 1 && !trackingList[0].trackingUrl.isNullOrBlank()) {
                                openTrackingUrl(trackingList[0].trackingUrl)
                            } else {
                                showTrackingDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Track Order",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

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
                is UiState.Success -> OrderDetailsContent(
                    order = s.data,
                    onOpenTrackingUrl = ::openTrackingUrl
                )
                else -> {}
            }
        }
    }

    if (showTrackingDialog && order != null) {
        val trackingList = order.trackingInformation.orEmpty()
        AlertDialog(
            onDismissRequest = { showTrackingDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Track Order",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    IconButton(
                        onClick = { showTrackingDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Order Number & Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order #${order.incrementId}",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        val statusColor = when (order.status?.lowercase()) {
                            "complete", "delivered" -> Color(0xFF2E7D32)
                            "canceled", "closed" -> Color(0xFFC62828)
                            else -> Color(0xFFF57C00)
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = order.status?.replaceFirstChar { it.uppercase() } ?: "Processing",
                                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                fontSize = 12.sp,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Progress Stepper
                    OrderTrackingTimeline(
                        orderStatus = order.status,
                        hasTracking = trackingList.isNotEmpty()
                    )

                    if (trackingList.isNotEmpty()) {
                        Text(
                            text = if (trackingList.size == 1) "Carrier Details" else "Select a carrier to track your shipment",
                            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        trackingList.forEach { info ->
                            val title = info.carrierTitle ?: info.carrierCode ?: "Carrier"
                            Surface(
                                color = Color(0xFFF9F9F9),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = title,
                                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    info.trackingNumber?.let { num ->
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Tracking #: $num",
                                            fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (!info.trackingUrl.isNullOrBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                showTrackingDialog = false
                                                openTrackingUrl(info.trackingUrl)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Black,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                text = "Track Package",
                                                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Explanatory note when unshipped
                        Surface(
                            color = Color(0xFFF9F9F9),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_truck),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFFF57C00)
                                )
                                Text(
                                    text = "Your order is currently being prepared. Tracking information will appear once your package is dispatched.",
                                    fontFamily = FontFamily(Font(R.font.gilroy_regular)),
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrackingDialog = false }) {
                    Text(
                        text = "Close",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        color = Color.Black
                    )
                }
            },
            dismissButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun OrderDetailsContent(
    order: OrderApiResponse,
    onOpenTrackingUrl: (String?) -> Unit
) {
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

        // Tracking Information (if available)
        val trackingList = order.trackingInformation.orEmpty()
        if (trackingList.isNotEmpty()) {
            item {
                SectionCard {
                    Text(
                        text = "TRACKING INFORMATION",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    trackingList.forEachIndexed { index, info ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color.LightGray.copy(0.5f)
                            )
                        }
                        val carrier = info.carrierTitle ?: info.carrierCode
                        if (!carrier.isNullOrBlank()) {
                            LabelValue("Carrier", carrier)
                        }
                        val number = info.trackingNumber
                        if (!number.isNullOrBlank()) {
                            LabelValue("Tracking #", number)
                        }
                        val url = info.trackingUrl
                        if (!url.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onOpenTrackingUrl(url) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    text = "Track Package",
                                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(0.5f))
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

@Composable
private fun OrderTrackingTimeline(
    orderStatus: String?,
    hasTracking: Boolean
) {
    val statusLower = orderStatus?.lowercase() ?: "processing"
    val isCanceled = statusLower in listOf("canceled", "closed")

    val steps = if (isCanceled) {
        listOf("Placed" to true, "Canceled" to true)
    } else {
        val isProcessing = statusLower in listOf("processing", "complete", "delivered", "shipped") || hasTracking
        val isShipped = statusLower in listOf("complete", "delivered", "shipped") || hasTracking
        val isDelivered = statusLower in listOf("complete", "delivered")
        listOf(
            "Placed" to true,
            "Processing" to isProcessing,
            "Shipped" to isShipped,
            "Delivered" to isDelivered
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, isCompleted) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                if (isCompleted) {
                    val iconTint = if (isCanceled && label == "Canceled") Color(0xFFC62828) else Color(0xFF2E7D32)
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(1.5.dp, Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.LightGray, CircleShape)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    fontSize = 11.sp,
                    color = if (isCompleted) Color.Black else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
            if (index < steps.size - 1) {
                val lineDone = steps[index + 1].second
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.7f)
                        .background(if (lineDone) Color(0xFF2E7D32) else Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
    }
}

