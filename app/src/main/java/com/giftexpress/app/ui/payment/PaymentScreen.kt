package com.giftexpress.app.ui.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.giftexpress.app.R
import com.giftexpress.app.ui.theme.Gilroy
import com.giftexpress.app.utils.UiState

private val AccentRed = Color(0xFFE53935)
private val MintBackground = Color(0xFFB2EAE3)
private val PayPalYellow = Color(0xFFFFC439)
private val AmazonOrange = Color(0xFFFF9900)
private val AfterpayMint = Color(0xFFB2FCE4)
private val AfterpayBlack = Color(0xFF000000)

@Composable
fun PaymentScreen(
    total: Double,
    stripeState: StripePaymentState,
    payPalState: PayPalPaymentState,
    redirectState: RedirectPaymentState,
    placeOrderState: UiState<*>,
    selectedMethod: PaymentMethodOption,
    onMethodSelected: (PaymentMethodOption) -> Unit,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val isLoading = stripeState is StripePaymentState.Loading
        || payPalState is PayPalPaymentState.Loading
        || payPalState is PayPalPaymentState.AwaitingApproval
        || redirectState is RedirectPaymentState.Loading
        || redirectState is RedirectPaymentState.AwaitingApproval
        || placeOrderState is UiState.Loading

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Payment Method",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFF8F8F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Total Amount bar (mint, like the mockup)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MintBackground, RoundedCornerShape(6.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Payable Amount",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "$${String.format("%.2f", total)}",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // PayPal Express Checkout
                PaymentMethodRow(
                    selected = selectedMethod == PaymentMethodOption.PAYPAL,
                    selectedBorderColor = PayPalYellow,
                    onClick = { onMethodSelected(PaymentMethodOption.PAYPAL) },
                    logo = { PayPalLogo() },
                    label = "PayPal Express Checkout"
                )

                // Google Pay (presented through the Stripe sheet)
                PaymentMethodRow(
                    selected = selectedMethod == PaymentMethodOption.GOOGLE_PAY,
                    selectedBorderColor = AccentRed,
                    onClick = { onMethodSelected(PaymentMethodOption.GOOGLE_PAY) },
                    logo = { GPayLogo() },
                    label = "Google Pay"
                )

                // Amazon Pay (hosted-page checkout)
                PaymentMethodRow(
                    selected = selectedMethod == PaymentMethodOption.AMAZON_PAY,
                    selectedBorderColor = AmazonOrange,
                    onClick = { onMethodSelected(PaymentMethodOption.AMAZON_PAY) },
                    logo = { AmazonPayLogo() },
                    label = "Amazon Pay"
                )

                // Afterpay (hosted-page checkout)
                PaymentMethodRow(
                    selected = selectedMethod == PaymentMethodOption.AFTERPAY,
                    selectedBorderColor = AfterpayMint,
                    onClick = { onMethodSelected(PaymentMethodOption.AFTERPAY) },
                    logo = { AfterpayLogo() },
                    label = "Afterpay"
                )

                // Klarna (via Stripe)
                PaymentMethodRow(
                    selected = selectedMethod == PaymentMethodOption.KLARNA,
                    selectedBorderColor = Color(0xFFFFB3C7), // Klarna Pink
                    onClick = { onMethodSelected(PaymentMethodOption.KLARNA) },
                    logo = { KlarnaLogo() },
                    label = "Klarna"
                )

                Divider(color = Color(0xFFE0E0E0), modifier = Modifier.padding(vertical = 4.dp))

                // Credit / Debit card (Stripe PaymentSheet)
                CardMethodSection(
                    selected = selectedMethod == PaymentMethodOption.CARD,
                    onClick = { onMethodSelected(PaymentMethodOption.CARD) }
                )

                // Error messages
                val errorMessage = when {
                    stripeState is StripePaymentState.Error -> stripeState.message
                    payPalState is PayPalPaymentState.Error -> payPalState.message
                    redirectState is RedirectPaymentState.Error -> redirectState.message
                    placeOrderState is UiState.Error -> (placeOrderState as UiState.Error).message
                    else -> null
                }
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
                    ) {
                        Text(
                            text = errorMessage,
                            fontFamily = Gilroy,
                            fontSize = 13.sp,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // Pay button (red, like the mockup)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Pay",
                                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", total)}",
                                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = when (selectedMethod) {
                        PaymentMethodOption.PAYPAL -> "Powered by PayPal"
                        PaymentMethodOption.AMAZON_PAY -> "Powered by Amazon Pay"
                        PaymentMethodOption.AFTERPAY -> "Powered by Afterpay"
                        PaymentMethodOption.KLARNA -> "Powered by Klarna"
                        else -> "Powered by Stripe"
                    },
                    fontFamily = Gilroy,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    selected: Boolean,
    selectedBorderColor: Color,
    onClick: () -> Unit,
    logo: @Composable () -> Unit,
    label: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) selectedBorderColor else Color(0xFFE0E0E0)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            logo()
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )
            SelectionIndicator(selected = selected, color = selectedBorderColor)
        }
    }
}

@Composable
private fun CardMethodSection(
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) AccentRed else Color(0xFFE0E0E0)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Credit Card / Debit Card",
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )
                SelectionIndicator(selected = selected, color = AccentRed)
            }
            if (selected) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Card details are entered on the secure Stripe payment sheet.",
                        fontFamily = Gilroy,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentBadge("VISA")
                    PaymentBadge("MC")
                    PaymentBadge("AMEX")
                }
            }
        }
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean, color: Color) {
    if (selected) {
        Box(
            modifier = Modifier.size(22.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(Color.White, CircleShape)
                .padding(1.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFFBDBDBD)),
                modifier = Modifier.fillMaxSize()
            ) {}
        }
    }
}

@Composable
private fun PayPalLogo() {
    Image(
        painter = painterResource(id = R.drawable.paypal_logo),
        contentDescription = "PayPal",
        modifier = Modifier.size(28.dp)
    )
}

@Composable
private fun AmazonPayLogo() {
    // amazonpzy_logo viewport is 60x19 — keep that aspect ratio at ~18dp tall
    Image(
        painter = painterResource(id = R.drawable.amazonpzy_logo),
        contentDescription = "Amazon Pay",
        modifier = Modifier.height(18.dp).width(57.dp)
    )
}

/** Text-drawn Afterpay logo — mint pill with the "afterpay" wordmark, avoids a bitmap asset. */
@Composable
private fun AfterpayLogo() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = AfterpayMint
    ) {
        Text(
            text = "afterpay",
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = AfterpayBlack,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GPayLogo() {
    // gpay_logo viewport is 66x26 — keep that aspect ratio at ~22dp tall
    Image(
        painter = painterResource(id = R.drawable.gpay_logo),
        contentDescription = "Google Pay",
        modifier = Modifier.height(22.dp).width(56.dp)
    )
}

@Composable
private fun PaymentBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontSize = 11.sp,
            color = Color(0xFF333333),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun KlarnaLogo() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFFFB3C7) // Klarna Pink
    ) {
        Text(
            text = "Klarna.",
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
