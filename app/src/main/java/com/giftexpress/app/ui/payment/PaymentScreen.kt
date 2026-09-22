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

                // Total Amount bar (matches iOS amountContainerView)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MintBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "$${String.format("%.2f", total)}",
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Option 1: PayPal (matches iOS paypalView)
                PaymentOptionRow(
                    selected = selectedMethod == PaymentMethodOption.PAYPAL,
                    onClick = { onMethodSelected(PaymentMethodOption.PAYPAL) },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.paypal_logo),
                            contentDescription = "PayPal",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = "PayPal"
                )

                // Option 2: Other Payment Methods (matches iOS stripeView)
                PaymentOptionRow(
                    selected = selectedMethod == PaymentMethodOption.OTHER,
                    onClick = { onMethodSelected(PaymentMethodOption.OTHER) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_credit_card),
                            contentDescription = "Other Payment Methods",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = "Other Payment Methods",
                    subtitle = "Card, AfterPay, Klarna, AmazonPay, Google Pay"
                )

                // Error messages
                val errorMessage = when {
                    stripeState is StripePaymentState.Error -> stripeState.message
                    payPalState is PayPalPaymentState.Error -> payPalState.message
                    redirectState is RedirectPaymentState.Error -> redirectState.message
                    placeOrderState is UiState.Error -> placeOrderState.message
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

            // Pay button (matches iOS payButton: red, cornerRadius 12, "Pay $XX.XX")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Pay $${String.format("%.2f", total)}",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (selectedMethod == PaymentMethodOption.PAYPAL) "Powered by PayPal" else "Powered by Stripe",
                    fontFamily = Gilroy,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun PaymentOptionRow(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) Color(0xFFFF9800) else Color(0xFFE0E0E0)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontFamily = Gilroy,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            SelectionIndicator(selected = selected, color = Color(0xFFFF9800))
        }
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean, color: Color = Color(0xFFFF9800)) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Transparent, CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(2.dp, color),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, CircleShape)
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Transparent, CircleShape)
                .padding(2.dp)
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
