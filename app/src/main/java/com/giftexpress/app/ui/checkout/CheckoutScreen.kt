package com.giftexpress.app.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.giftexpress.app.R
import com.giftexpress.app.data.model.CartItemDetail
import com.giftexpress.app.data.model.CustomerAddressModel
import com.giftexpress.app.data.model.OrderTotals
import com.giftexpress.app.data.model.ShippingMethod
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

private val AccentRed = Color(0xFFE53935)
private val AccentOrange = Color(0xFFF5A623)
private val StepPink = Color(0xFFF8BBD0)
private val AddressBlue = Color(0xFFE8F4FD)
private val TotalsMint = Color(0xFFD7F4F0)
private val RowGray = Color(0xFFF4F1F1)
private val BorderGray = Color(0xFFE0E0E0)
private val GilroyBold = FontFamily(Font(R.font.gilroy_bold))

@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onChangeAddress: () -> Unit,
    onProceedToPayment: (Double) -> Unit,
    onProductClick: (String) -> Unit
) {
    val shippingMethodsState by viewModel.shippingMethods.collectAsState()
    val orderTotalsState by viewModel.orderTotalsState.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val selectedShippingMethod by viewModel.selectedShippingMethod.collectAsState()
    val couponCode by viewModel.couponCode.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponState by viewModel.couponState.collectAsState()
    val rewardsApplied by viewModel.rewardsApplied.collectAsState()
    val rewardsState by viewModel.rewardsState.collectAsState()
    val rewardsInput by viewModel.rewardsInput.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartItemUpdating by viewModel.cartItemUpdating.collectAsState()
    val isProtectionSelected by viewModel.isProtectionSelected.collectAsState()

    val totals: OrderTotals? = (orderTotalsState as? UiState.Success)?.data
    val grandTotal = totals?.grandTotal ?: 0.0
    val isLoading = orderTotalsState is UiState.Loading || shippingMethodsState is UiState.Loading

    var doNotSendInvoice by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = Color.White) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "Order Summary",
                            fontFamily = GilroyBold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                    CheckoutStepper(currentStep = 2)
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Button(
                    onClick = { onProceedToPayment(grandTotal) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedAddress != null && selectedShippingMethod != null && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$${String.format("%.2f", grandTotal)}",
                                    fontFamily = GilroyBold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Divider(
                                modifier = Modifier.height(24.dp).width(1.dp),
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "CONTINUE",
                                    fontFamily = GilroyBold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Deliver to + address card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Deliver to", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    OutlinedButton(
                        onClick = onChangeAddress,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Change Address", fontFamily = Gilroy, fontSize = 13.sp, color = AccentRed)
                    }
                }
            }
            item { AddressCard(address = selectedAddress, onSelectClick = onChangeAddress) }

            // Shipping Method
            item {
                Text("Shipping Method", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }
            item {
                ShippingMethodSection(
                    state = shippingMethodsState,
                    selectedMethod = selectedShippingMethod,
                    onSelect = { viewModel.selectShippingMethod(it) }
                )
            }

            // Order Summary header + items
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order Summary", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    if (cartItems.isNotEmpty()) {
                        Text(
                            "${cartItems.size} Items in Cart",
                            fontFamily = Gilroy,
                            fontSize = 13.sp,
                            color = AccentOrange
                        )
                    }
                }
            }

            // Reward points banner (Amasty highlight from totals)
            val highlightText = totals?.extensionAttributes?.rewardsHighlight
                ?.takeIf { it.visible == true }?.captionText
            if (!highlightText.isNullOrBlank()) {
                item { RewardsBanner(text = highlightText) }
            }

            items(cartItems.size) { index ->
                OrderItemCard(
                    item = cartItems[index],
                    enabled = !cartItemUpdating,
                    onQtyChange = { newQty -> viewModel.updateItemQty(cartItems[index], newQty) },
                    onRemove = { viewModel.removeItem(cartItems[index]) },
                    onProductClick = onProductClick
                )
            }

            // Totals card
            item {
                TotalsCard(
                    totalsState = orderTotalsState,
                    shippingMethodTitle = selectedShippingMethod?.displayTitle,
                    onRetry = { viewModel.getCartTotals() }
                )
            }

            // Shipping Protection
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Shipping Protection", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleShippingProtection() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircleCheckbox(checked = isProtectionSelected, color = Color(0xFF4CD964))
                        Text("Shipping Protection", fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Immediate, guaranteed return for lost or damaged items*.", fontFamily = Gilroy, fontSize = 13.sp, color = Color.Gray)
                }
            }

            // Do not send invoice
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { doNotSendInvoice = !doNotSendInvoice },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircleCheckbox(checked = doNotSendInvoice, color = AccentRed)
                    Text("Do not Send Invoice", fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black)
                }
            }

            // Apply Discount Code
            item {
                DiscountSection(
                    couponCode = couponCode,
                    appliedCoupon = appliedCoupon,
                    couponState = couponState,
                    onCodeChange = { viewModel.updateCouponCode(it) },
                    onApply = { viewModel.applyCoupon() },
                    onRemove = { viewModel.removeCoupon() }
                )
            }

            // Apply Rewards
            item {
                RewardsSection(
                    rewardsInput = rewardsInput,
                    rewardsApplied = rewardsApplied,
                    rewardsState = rewardsState,
                    onInputChange = { viewModel.updateRewardsInput(it) },
                    onApply = { rewardsInput.toIntOrNull()?.let { viewModel.applyRewards(it) } },
                    onRemove = { viewModel.removeRewards() }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/** 1 ✓ ── 2 ── 3 progress header (step 2 active on this screen). */
@Composable
private fun CheckoutStepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step 1 — completed
        Box(
            modifier = Modifier.size(34.dp).background(StepPink, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
        }
        Divider(modifier = Modifier.weight(1f), thickness = 2.dp, color = AccentRed)
        // Step 2 — active
        Box(
            modifier = Modifier.size(34.dp).background(if (currentStep >= 2) Color(0xFFD32F2F) else Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("2", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }
        Divider(modifier = Modifier.weight(1f), thickness = 2.dp, color = BorderGray)
        // Step 3 — upcoming
        Box(
            modifier = Modifier.size(34.dp).background(Color.White, CircleShape).border(1.5.dp, BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("3", fontFamily = GilroyBold, fontSize = 15.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun AddressCard(address: CustomerAddressModel?, onSelectClick: () -> Unit) {
    if (address == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AddressBlue, RoundedCornerShape(8.dp))
                .clickable(onClick = onSelectClick)
                .padding(16.dp)
        ) {
            Text("+ Select a delivery address", fontFamily = Gilroy, fontSize = 15.sp, color = AccentRed)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AddressBlue, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(address.fullName, fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Text(
                address.formattedAddress,
                fontFamily = Gilroy,
                fontSize = 13.sp,
                color = Color(0xFF444444),
                lineHeight = 19.sp
            )
            address.telephone?.takeIf { it.isNotBlank() }?.let {
                Text("T: $it", fontFamily = Gilroy, fontSize = 13.sp, color = Color(0xFF444444))
            }
        }
    }
}

@Composable
private fun ShippingMethodSection(
    state: UiState<List<ShippingMethod>>,
    selectedMethod: ShippingMethod?,
    onSelect: (ShippingMethod) -> Unit
) {
    when (state) {
        is UiState.Loading -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF333333), strokeWidth = 2.dp)
                Text("Loading shipping methods...", fontFamily = Gilroy, fontSize = 13.sp, color = Color.Gray)
            }
        }
        is UiState.Error -> Text(state.message, color = AccentRed, fontFamily = Gilroy, fontSize = 14.sp)
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                Text("No shipping methods available", fontFamily = Gilroy, fontSize = 14.sp, color = Color.Gray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.data.forEach { method ->
                        val selected = selectedMethod?.carrierCode == method.carrierCode
                            && selectedMethod?.methodCode == method.methodCode
                        ShippingMethodRow(method = method, selected = selected, onSelect = { onSelect(method) })
                    }
                }
            }
        }
        else -> Text("Select an address to see shipping options", fontFamily = Gilroy, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun ShippingMethodRow(method: ShippingMethod, selected: Boolean, onSelect: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color.White else RowGray,
        border = if (selected) BorderStroke(1.dp, BorderGray) else null,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier.size(22.dp).background(AccentOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            } else {
                Box(modifier = Modifier.size(22.dp).background(Color.White, CircleShape))
            }
            Text(
                method.displayTitle,
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$${String.format("%.2f", method.displayAmount)}",
                fontFamily = GilroyBold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun RewardsBanner(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFFF7C948), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6B4E00))
        }
        Text(text, fontFamily = Gilroy, fontSize = 13.sp, color = Color.Black, lineHeight = 18.sp)
    }
}

@Composable
private fun OrderItemCard(
    item: CartItemDetail,
    enabled: Boolean,
    onQtyChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onProductClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            AsyncImage(
                model = item.image,
                contentDescription = item.name,
                modifier = Modifier
                    .size(76.dp)
                    .clickable { item.sku?.let { onProductClick(it) } },
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    item.name ?: "",
                    fontFamily = GilroyBold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 2,
                    modifier = Modifier.clickable { item.sku?.let { onProductClick(it) } }
                )
                Text(
                    "$${String.format("%.2f", item.price ?: 0.0)}",
                    fontFamily = GilroyBold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AccentRed
                )
                item.sku?.let { Text("Sku: $it", fontFamily = Gilroy, fontSize = 12.sp, color = Color(0xFF555555)) }
                item.size?.takeIf { it.isNotBlank() }?.let {
                    Text("Size: $it", fontFamily = Gilroy, fontSize = 12.sp, color = Color(0xFF555555))
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onRemove, enabled = enabled, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(10.dp))
                QtyStepper(qty = item.qty ?: 1, enabled = enabled, onQtyChange = onQtyChange)
            }
        }
    }
}

@Composable
private fun QtyStepper(qty: Int, enabled: Boolean, onQtyChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "-",
            fontFamily = GilroyBold,
            fontSize = 18.sp,
            color = AccentRed,
            modifier = Modifier
                .clickable(enabled = enabled && qty > 1) { onQtyChange(qty - 1) }
                .padding(horizontal = 10.dp, vertical = 2.dp)
        )
        Text(
            "$qty",
            fontFamily = GilroyBold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        Text(
            "+",
            fontFamily = GilroyBold,
            fontSize = 16.sp,
            color = AccentRed,
            modifier = Modifier
                .clickable(enabled = enabled) { onQtyChange(qty + 1) }
                .padding(horizontal = 10.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TotalsCard(
    totalsState: UiState<OrderTotals>,
    shippingMethodTitle: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD4F2EB), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        when (totalsState) {
            is UiState.Loading, is UiState.Idle -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF333333), strokeWidth = 2.dp)
                    Text("Calculating total...", fontFamily = Gilroy, fontSize = 13.sp, color = Color.Gray)
                }
            }
            is UiState.Error -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Unable to calculate totals: ${totalsState.message}",
                        fontFamily = Gilroy,
                        fontSize = 13.sp,
                        color = AccentRed
                    )
                    TextButton(onClick = onRetry) {
                        Text("RETRY", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentRed)
                    }
                }
            }
            is UiState.Success -> {
                val totals = totalsState.data
                TotalsRow("Subtotal", totals.subtotal ?: 0.0)
                Spacer(Modifier.height(10.dp))
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val shippingLabel = if (shippingMethodTitle != null) "Shipping & Handling ($shippingMethodTitle)" else "Shipping & Handling"
                        Text(shippingLabel, fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black, modifier = Modifier.weight(1f))
                        Text(
                            "$${String.format("%.2f", totals.shippingAmount ?: 0.0)}",
                            fontFamily = Gilroy,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
                if ((totals.taxAmount ?: 0.0) != 0.0) {
                    Spacer(Modifier.height(10.dp))
                    TotalsRow("Tax", totals.taxAmount ?: 0.0)
                }
                if ((totals.discountAmount ?: 0.0) != 0.0) {
                    Spacer(Modifier.height(10.dp))
                    TotalsRow("Discount", -(Math.abs(totals.discountAmount ?: 0.0)))
                }
                
                val amastySegment = totals.totalSegments?.find { it.code == "amasty_extrafee" }
                val protectionFee = amastySegment?.extensionAttributes?.taxAmastyExtrafeeDetails?.valueInclTax
                    ?: amastySegment?.value ?: 0.0
                if (protectionFee > 0.0) {
                    Spacer(Modifier.height(10.dp))
                    TotalsRow("Shipping Protection", protectionFee)
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Color.Black)
                    Text(
                        "$${String.format("%.2f", totals.grandTotal ?: 0.0)}",
                        fontFamily = GilroyBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalsRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = Gilroy, fontSize = 14.sp, color = Color.Black, modifier = Modifier.weight(1f))
        val prefix = if (amount < 0) "-" else ""
        Text(
            "$prefix$${String.format("%.2f", Math.abs(amount))}",
            fontFamily = Gilroy,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun CircleCheckbox(checked: Boolean, color: Color) {
    if (checked) {
        Box(
            modifier = Modifier.size(22.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    } else {
        Box(modifier = Modifier.size(22.dp).border(1.5.dp, Color(0xFFBDBDBD), CircleShape))
    }
}

@Composable
private fun DiscountSection(
    couponCode: String,
    appliedCoupon: String,
    couponState: UiState<Boolean>,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    onRemove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(26.dp).background(AccentOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("%", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
            Text("Apply Discount Code", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        }
        if (appliedCoupon.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Applied: $appliedCoupon", fontFamily = GilroyBold, fontSize = 14.sp, color = Color(0xFF4CAF50))
                TextButton(onClick = onRemove) { Text("Remove", color = AccentRed, fontFamily = Gilroy) }
            }
        } else {
            OutlinedTextField(
                value = couponCode,
                onValueChange = onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter Discount Code", fontFamily = Gilroy, fontSize = 13.sp, color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = BorderGray
                ),
                trailingIcon = {
                    TextButton(
                        onClick = onApply,
                        enabled = couponCode.isNotBlank() && couponState !is UiState.Loading
                    ) {
                        if (couponState is UiState.Loading) {
                            CircularProgressIndicator(color = AccentOrange, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("APPLY", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentOrange)
                        }
                    }
                }
            )
            if (couponState is UiState.Error) {
                Text(couponState.message, color = AccentRed, fontFamily = Gilroy, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RewardsSection(
    rewardsInput: String,
    rewardsApplied: Boolean,
    rewardsState: UiState<Boolean>,
    onInputChange: (String) -> Unit,
    onApply: () -> Unit,
    onRemove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("🏆", fontSize = 18.sp)
            Text("Apply Rewards", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
        }
        if (rewardsApplied) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reward points applied", fontFamily = GilroyBold, fontSize = 14.sp, color = Color(0xFF4CAF50))
                TextButton(onClick = onRemove) { Text("Remove", color = AccentRed, fontFamily = Gilroy) }
            }
        } else {
            OutlinedTextField(
                value = rewardsInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0.00", fontFamily = Gilroy, fontSize = 13.sp, color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = BorderGray
                ),
                trailingIcon = {
                    TextButton(
                        onClick = onApply,
                        enabled = rewardsInput.isNotBlank() && rewardsState !is UiState.Loading
                    ) {
                        if (rewardsState is UiState.Loading) {
                            CircularProgressIndicator(color = AccentOrange, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("APPLY", fontFamily = GilroyBold, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentOrange)
                        }
                    }
                }
            )
            if (rewardsState is UiState.Error) {
                Text(rewardsState.message, color = AccentRed, fontFamily = Gilroy, fontSize = 12.sp)
            }
        }
    }
}
